package com.example.course_system.service;

import com.example.course_system.entity.*;
import com.example.course_system.exception.BusinessException;
import com.example.course_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepo;
    @Autowired
    private StudentRepository studentRepo;
    @Autowired
    private TeacherRepository teacherRepo;
    @Autowired
    private EnrollmentRepository enrollRepo;

    // ===== 课程管理 =====

    @Cacheable(value = "courses", key = "'all'")
    public List<Course> getAllCourses() {
        System.out.println(">>> 从数据库查询所有课程（缓存未命中）");
        return courseRepo.findAll();
    }

    @CacheEvict(value = "courses", allEntries = true)
    public Course addCourse(Course course) {
        if (courseRepo.existsById(course.getCourseId())) {
            throw new BusinessException("课程编号已存在");
        }
        return courseRepo.save(course);
    }

    @CacheEvict(value = "courses", allEntries = true)
    @Transactional
    public void deleteCourse(String courseId) {
        List<Enrollment> enrolls = enrollRepo.findByCourseId(courseId);
        if (!enrolls.isEmpty()) {
            enrollRepo.deleteAll(enrolls);
        }
        courseRepo.deleteById(courseId);
    }

    public Course getCourseById(String courseId) {
        return courseRepo.findById(courseId).orElse(null);
    }

    public void updateCourse(Course course) {
        courseRepo.save(course);
    }

    @CacheEvict(value = "courses", allEntries = true)
    @Transactional
    public Course assignTeacherToCourse(String courseId, String teacherId) {
        Course course = courseRepo.findById(courseId).orElse(null);
        if (course == null) throw new BusinessException("课程不存在");
        Teacher teacher = teacherRepo.findById(teacherId).orElse(null);
        if (teacher == null) throw new BusinessException("教师不存在");
        course.setTeacherId(teacherId);
        course.setIsArrangeTeacher(true);
        return courseRepo.save(course);
    }

    @CacheEvict(value = "courses", allEntries = true)
    @Transactional
    public Course unassignTeacher(String courseId) {
        Course course = courseRepo.findById(courseId).orElse(null);
        if (course == null) throw new BusinessException("课程不存在");
        course.setTeacherId(null);
        course.setIsArrangeTeacher(false);
        return courseRepo.save(course);
    }

    // ===== 学生选课 =====

    /**
     * 学生选课。
     *
     * 容量校验用带悲观锁的查询读取课程，把同一门课的并发选课请求串行化。
     *
     * 隔离级别显式设为 READ_COMMITTED：MySQL 默认的 REPEATABLE-READ 下，
     * 事务内的普通 SELECT 会读取事务开始时的快照，导致排队等待的请求
     * 仍然用过期的人数做容量判断（实测 200 并发、Ramp-up 0 时仍会超出 9 人）。
     * READ_COMMITTED 下每条查询都会读取最新的已提交数据，配合行锁才能正确拦截。
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Enrollment selectCourse(String studentId, String courseId) {
        List<Enrollment> existing = enrollRepo.findByStudentId(studentId);
        if (existing.size() >= 5) {
            throw new BusinessException("选课已达上限5门");
        }
        for (Enrollment e : existing) {
            if (e.getCourseId().equals(courseId)) {
                throw new BusinessException("已选该课程");
            }
        }
        // 加写锁读取课程，保证同一门课的容量校验与插入是串行的
        Course course = courseRepo.findByIdForUpdate(courseId).orElse(null);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        long enrolled = enrollRepo.countByCourseId(courseId);
        if (course.getCapacity() != null && enrolled >= course.getCapacity()) {
            throw new BusinessException("该课程已满，无法选课");
        }
        Enrollment enroll = new Enrollment();
        enroll.setStudentId(studentId);
        enroll.setCourseId(courseId);
        enroll.setGrade(null);  // 成绩未录入统一用 null 表示
        try {
            return enrollRepo.save(enroll);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("已选该课程（并发拦截）");
        }
    }

    @Transactional
    public void dropCourse(String studentId, String courseId) {
        List<Enrollment> list = enrollRepo.findByStudentId(studentId);
        boolean exists = false;
        for (Enrollment e : list) {
            if (e.getCourseId().equals(courseId)) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            throw new BusinessException("未选此课程，无法退课");
        }
        enrollRepo.deleteByStudentIdAndCourseId(studentId, courseId);
    }

    // ===== 教师录入成绩 =====

    @Transactional
    public void enterGrade(String teacherId, String studentId, String courseId, int grade) {
        if (grade < 0 || grade > 100) {
            throw new BusinessException("成绩必须在0-100之间");
        }
        Course course = courseRepo.findById(courseId).orElse(null);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        if (!teacherId.equals(course.getTeacherId())) {
            throw new BusinessException("无权给非本人授课的课程录入成绩");
        }
        List<Enrollment> list = enrollRepo.findByStudentId(studentId);
        for (Enrollment e : list) {
            if (e.getCourseId().equals(courseId)) {
                e.setGrade(grade);
                enrollRepo.save(e);
                return;
            }
        }
        throw new BusinessException("该学生未选此课程");
    }

    // ===== 查询 =====

    public List<Student> getStudentsByCourse(String courseId) {
        List<Enrollment> enrolls = enrollRepo.findByCourseId(courseId);
        return enrolls.stream()
                .map(e -> studentRepo.findById(e.getStudentId()).orElse(null))
                .filter(s -> s != null)
                .collect(Collectors.toList());
    }

    public List<Enrollment> getEnrollmentsByStudent(String studentId) {
        return enrollRepo.findByStudentId(studentId);
    }
}
