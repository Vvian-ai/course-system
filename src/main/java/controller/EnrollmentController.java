package com.example.course_system.controller;

import com.example.course_system.common.Result;
import com.example.course_system.dto.EnrollmentDTO;
import com.example.course_system.entity.Enrollment;
import com.example.course_system.repository.EnrollmentRepository;
import com.example.course_system.service.CourseService;
import com.example.course_system.util.DtoConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "选课与成绩", description = "学生选课、退课、成绩查询与录入接口")
@RestController
@RequestMapping("/api/enrollments")
@Validated
public class EnrollmentController {

    @Autowired
    private CourseService courseService;
    @Autowired
    private EnrollmentRepository enrollRepo;

    @Operation(summary = "查询某学生的选课记录")
    @GetMapping("/student/{studentId}")
    public Result<List<EnrollmentDTO>> getStudentEnrollments(
            HttpServletRequest request,
            @Parameter(description = "学号") @PathVariable String studentId) {

        String currentUser = (String) request.getAttribute("username");
        String currentRole = (String) request.getAttribute("role");

        // 学生只能查自己的
        if ("student".equals(currentRole) && !currentUser.equals(studentId)) {
            return Result.error(403, "无权查看他人选课记录");
        }

        return Result.success(DtoConverter.toEnrollmentDTOList(courseService.getEnrollmentsByStudent(studentId)));
    }

    @Operation(summary = "查询某课程的选课学生名单")
    @GetMapping("/byCourse")
    public Result<List<EnrollmentDTO>> getEnrollmentsByCourse(
            @Parameter(description = "课程编号") @RequestParam @NotBlank(message = "课程编号不能为空") String courseId) {
        return Result.success(DtoConverter.toEnrollmentDTOList(enrollRepo.findByCourseId(courseId)));
    }

    @Operation(summary = "学生选课", description = "学生用自己的 Token，管理员需要传 studentId")
    @PostMapping
    public Result<EnrollmentDTO> selectCourse(
            HttpServletRequest request,
            @Parameter(description = "课程编号") @RequestParam @NotBlank(message = "课程编号不能为空") String courseId,
            @Parameter(description = "学号（仅管理员选课时用，学生不用传）") @RequestParam(required = false) String studentId) {

        String currentUser = (String) request.getAttribute("username");
        String currentRole = (String) request.getAttribute("role");

        String targetStudentId;
        if ("admin".equals(currentRole)) {
            // 管理员：必须传 studentId
            if (studentId == null || studentId.isEmpty()) {
                return Result.error(400, "管理员选课时必须指定学生学号");
            }
            targetStudentId = studentId;
        } else {
            // 学生：用 Token 里的学号
            targetStudentId = currentUser;
        }

        Enrollment enroll = courseService.selectCourse(targetStudentId, courseId);
        return Result.success("选课成功", DtoConverter.toEnrollmentDTO(enroll));
    }

    @Operation(summary = "学生退课", description = "学号从 Token 自动获取")
    @DeleteMapping
    public Result<String> dropCourse(
            HttpServletRequest request,
            @Parameter(description = "课程编号") @RequestParam @NotBlank(message = "课程编号不能为空") String courseId) {
        String studentId = (String) request.getAttribute("username");
        courseService.dropCourse(studentId, courseId);
        return Result.success("退课成功");
    }

    @Operation(summary = "教师录入成绩", description = "只能给自己教的课打分，成绩范围 0-100")
    @PutMapping("/grade")
    public Result<String> enterGrade(
            HttpServletRequest request,
            @Parameter(description = "学号") @RequestParam @NotBlank(message = "学号不能为空") String studentId,
            @Parameter(description = "课程编号") @RequestParam @NotBlank(message = "课程编号不能为空") String courseId,
            @Parameter(description = "成绩") @RequestParam @NotNull(message = "成绩不能为空") @Min(value = 0, message = "成绩不能小于0") @Max(value = 100, message = "成绩不能大于100") int grade) {
        String teacherId = (String) request.getAttribute("username");
        courseService.enterGrade(teacherId, studentId, courseId, grade);
        return Result.success("成绩录入成功");
    }
}