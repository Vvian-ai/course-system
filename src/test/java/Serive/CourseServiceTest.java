package com.example.course_system.service;

import com.example.course_system.entity.Course;
import com.example.course_system.entity.Enrollment;
import com.example.course_system.entity.Student;
import com.example.course_system.entity.Teacher;
import com.example.course_system.repository.CourseRepository;
import com.example.course_system.repository.EnrollmentRepository;
import com.example.course_system.repository.StudentRepository;
import com.example.course_system.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseService 核心业务单元测试")
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepo;

    @Mock
    private StudentRepository studentRepo;

    @Mock
    private TeacherRepository teacherRepo;

    @Mock
    private EnrollmentRepository enrollRepo;

    @InjectMocks
    private CourseService courseService;

    private Course testCourse;
    private Student testStudent;
    private Teacher testTeacher;

    @BeforeEach
    void setUp() {
        testCourse = new Course("001", "高等数学", 5, null, false);
        testCourse.setCapacity(50);
        testStudent = new Student("1001", "张三", "计算机科学");
        testTeacher = new Teacher("2001", "王五");
    }

    // ==================== 学生选课测试 ====================

    @Test
    @DisplayName("选课成功 - 正常情况")
    void testSelectCourse_Success() {
        List<Enrollment> existing = new ArrayList<>();
        existing.add(createEnrollment("1001", "002"));

        when(enrollRepo.findByStudentId("1001")).thenReturn(existing);
        when(courseRepo.findById("001")).thenReturn(Optional.of(testCourse));
        when(enrollRepo.countByCourseId("001")).thenReturn(0L);
        when(enrollRepo.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Enrollment result = courseService.selectCourse("1001", "001");

        assertNotNull(result);
        assertEquals("1001", result.getStudentId());
        assertEquals("001", result.getCourseId());
        assertNull(result.getGrade());
        verify(enrollRepo, times(1)).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("选课失败 - 超过5门上限")
    void testSelectCourse_ExceedLimit() {
        List<Enrollment> existing = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            existing.add(createEnrollment("1001", "00" + i));
        }

        when(enrollRepo.findByStudentId("1001")).thenReturn(existing);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> courseService.selectCourse("1001", "006"));
        assertEquals("选课已达上限5门", ex.getMessage());
        verify(enrollRepo, never()).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("选课失败 - 重复选课")
    void testSelectCourse_Duplicate() {
        List<Enrollment> existing = new ArrayList<>();
        existing.add(createEnrollment("1001", "001"));

        when(enrollRepo.findByStudentId("1001")).thenReturn(existing);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> courseService.selectCourse("1001", "001"));
        assertEquals("已选该课程", ex.getMessage());
        verify(enrollRepo, never()).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("选课失败 - 课程不存在")
    void testSelectCourse_CourseNotFound() {
        when(enrollRepo.findByStudentId("1001")).thenReturn(new ArrayList<>());
        when(courseRepo.findById("999")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> courseService.selectCourse("1001", "999"));
        assertEquals("课程不存在", ex.getMessage());
    }

    @Test
    @DisplayName("选课失败 - 课程已满")
    void testSelectCourse_CourseFull() {
        // 课程容量 1
        testCourse.setCapacity(1);

        when(enrollRepo.findByStudentId("1001")).thenReturn(new ArrayList<>());
        when(courseRepo.findById("001")).thenReturn(Optional.of(testCourse));
        when(enrollRepo.countByCourseId("001")).thenReturn(1L);  // 已选 1 人

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> courseService.selectCourse("1001", "001"));
        assertEquals("该课程已满，无法选课", ex.getMessage());
        verify(enrollRepo, never()).save(any(Enrollment.class));
    }

    // ==================== 退课测试 ====================

    @Test
    @DisplayName("退课成功")
    void testDropCourse_Success() {
        Enrollment enrollment = createEnrollment("1001", "001");
        List<Enrollment> list = new ArrayList<>();
        list.add(enrollment);

        when(enrollRepo.findByStudentId("1001")).thenReturn(list);

        courseService.dropCourse("1001", "001");
        verify(enrollRepo, times(1)).deleteByStudentIdAndCourseId("1001", "001");
    }

    @Test
    @DisplayName("退课失败 - 未选此课程")
    void testDropCourse_NotEnrolled() {
        when(enrollRepo.findByStudentId("1001")).thenReturn(new ArrayList<>());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> courseService.dropCourse("1001", "001"));
        assertEquals("未选此课程，无法退课", ex.getMessage());
    }

    // ==================== 成绩录入测试 ====================

    @Test
    @DisplayName("成绩录入成功 - 正常分数")
    void testEnterGrade_Success() {
        testCourse.setTeacherId("2001");
        when(courseRepo.findById("001")).thenReturn(Optional.of(testCourse));

        Enrollment enrollment = createEnrollment("1001", "001");
        List<Enrollment> list = new ArrayList<>();
        list.add(enrollment);

        when(enrollRepo.findByStudentId("1001")).thenReturn(list);
        when(enrollRepo.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        courseService.enterGrade("2001", "1001", "001", 85);

        assertEquals(85, enrollment.getGrade());
        verify(enrollRepo, times(1)).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("成绩录入失败 - 成绩大于100")
    void testEnterGrade_GradeTooHigh() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> courseService.enterGrade("2001", "1001", "001", 150));
        assertEquals("成绩必须在0-100之间", ex.getMessage());
        verify(enrollRepo, never()).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("成绩录入失败 - 成绩小于0")
    void testEnterGrade_GradeTooLow() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> courseService.enterGrade("2001", "1001", "001", -10));
        assertEquals("成绩必须在0-100之间", ex.getMessage());
        verify(enrollRepo, never()).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("成绩录入失败 - 教师无权给非本人授课课程打分")
    void testEnterGrade_NotTeacherCourse() {
        testCourse.setTeacherId("2001");
        when(courseRepo.findById("001")).thenReturn(Optional.of(testCourse));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> courseService.enterGrade("2002", "1001", "001", 85));
        assertEquals("无权给非本人授课的课程录入成绩", ex.getMessage());
    }

    @Test
    @DisplayName("成绩录入失败 - 学生未选该课程")
    void testEnterGrade_NotEnrolled() {
        testCourse.setTeacherId("2001");
        when(courseRepo.findById("001")).thenReturn(Optional.of(testCourse));

        List<Enrollment> list = new ArrayList<>();
        list.add(createEnrollment("1001", "002"));

        when(enrollRepo.findByStudentId("1001")).thenReturn(list);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> courseService.enterGrade("2001", "1001", "001", 85));
        assertEquals("该学生未选此课程", ex.getMessage());
    }

    // ==================== 教师分配测试 ====================

    @Test
    @DisplayName("分配教师成功")
    void testAssignTeacher_Success() {
        when(courseRepo.findById("001")).thenReturn(Optional.of(testCourse));
        when(teacherRepo.findById("2001")).thenReturn(Optional.of(testTeacher));
        when(courseRepo.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Course result = courseService.assignTeacherToCourse("001", "2001");

        assertNotNull(result);
        assertEquals("2001", result.getTeacherId());
        assertTrue(result.getIsArrangeTeacher());
        verify(courseRepo, times(1)).save(any(Course.class));
    }

    @Test
    @DisplayName("分配教师失败 - 课程不存在")
    void testAssignTeacher_CourseNotFound() {
        when(courseRepo.findById("999")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> courseService.assignTeacherToCourse("999", "2001"));
        assertEquals("课程不存在", ex.getMessage());
    }

    @Test
    @DisplayName("分配教师失败 - 教师不存在")
    void testAssignTeacher_TeacherNotFound() {
        when(courseRepo.findById("001")).thenReturn(Optional.of(testCourse));
        when(teacherRepo.findById("9999")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> courseService.assignTeacherToCourse("001", "9999"));
        assertEquals("教师不存在", ex.getMessage());
    }

    // ==================== 辅助方法 ====================

    private Enrollment createEnrollment(String studentId, String courseId) {
        Enrollment e = new Enrollment();
        e.setStudentId(studentId);
        e.setCourseId(courseId);
        e.setGrade(null);
        return e;
    }
}