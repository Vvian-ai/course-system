package com.example.course_system.controller;

import com.example.course_system.common.Result;
import com.example.course_system.dto.StudentDTO;
import com.example.course_system.entity.Course;
import com.example.course_system.entity.Enrollment;
import com.example.course_system.entity.Student;
import com.example.course_system.repository.CourseRepository;
import com.example.course_system.repository.EnrollmentRepository;
import com.example.course_system.repository.StudentRepository;
import com.example.course_system.util.DtoConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "学生管理", description = "学生信息的增删改查接口")
@RestController
@RequestMapping("/api/students")
@Validated
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Operation(summary = "查询所有学生")
    @GetMapping
    public Result<List<StudentDTO>> getAllStudents(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        boolean canSeeEnrollments = "admin".equals(role) || "teacher".equals(role);

        List<Student> students = studentRepository.findAll();
        List<StudentDTO> dtos = DtoConverter.toStudentDTOList(students);

        for (StudentDTO dto : dtos) {
            if (!canSeeEnrollments) break;

            List<Enrollment> enrollments = enrollmentRepository.findByStudentId(dto.getStudentId());
            List<String> courseNames = new ArrayList<>();
            for (Enrollment e : enrollments) {
                Course course = courseRepository.findById(e.getCourseId()).orElse(null);
                if (course != null) {
                    courseNames.add(course.getCourseName());
                } else {
                    courseNames.add(e.getCourseId());
                }
            }
            dto.setEnrolledCourseNames(courseNames);
        }

        return Result.success(dtos);
    }

    @Operation(summary = "按学号查询学生")
    @GetMapping("/{studentId}")
    public Result<StudentDTO> getStudentById(
            @Parameter(description = "学号") @PathVariable String studentId) {
        Student student = studentRepository.findById(studentId).orElse(null);
        if (student == null) return Result.error(404, "学生不存在");
        return Result.success(DtoConverter.toStudentDTO(student));
    }

    @Operation(summary = "添加学生", description = "需要管理员权限，密码可选，默认123456")
    @PostMapping
    public Result<StudentDTO> addStudent(
            @Parameter(description = "学号") @RequestParam @NotBlank(message = "学号不能为空") String studentId,
            @Parameter(description = "姓名") @RequestParam @NotBlank(message = "姓名不能为空") String name,
            @Parameter(description = "专业") @RequestParam @NotBlank(message = "专业不能为空") String major,
            @Parameter(description = "密码（可选，默认123456）") @RequestParam(required = false) String password) {
        Student student = new Student();
        student.setStudentId(studentId);
        student.setName(name);
        student.setMajor(major);
        String rawPassword = (password != null && !password.isEmpty()) ? password : "123456";
        student.setPassword(passwordEncoder.encode(rawPassword));
        Student saved = studentRepository.save(student);
        return Result.success("学生添加成功", DtoConverter.toStudentDTO(saved));
    }

    @Operation(summary = "删除学生", description = "需要管理员权限，会级联删除选课记录")
    @DeleteMapping("/{studentId}")
    public Result<String> deleteStudent(
            @Parameter(description = "学号") @PathVariable String studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        if (!enrollments.isEmpty()) enrollmentRepository.deleteAll(enrollments);
        if (studentRepository.existsById(studentId)) {
            studentRepository.deleteById(studentId);
            return Result.success("学生删除成功，已清理其选课记录");
        }
        return Result.error(404, "学生不存在");
    }
}