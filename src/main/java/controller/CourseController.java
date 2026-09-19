package com.example.course_system.controller;

import com.example.course_system.common.Result;
import com.example.course_system.dto.CourseDTO;
import com.example.course_system.entity.Course;
import com.example.course_system.entity.Teacher;
import com.example.course_system.repository.EnrollmentRepository;
import com.example.course_system.repository.TeacherRepository;
import com.example.course_system.service.CourseService;
import com.example.course_system.util.DtoConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "课程管理", description = "课程的增删改查、分配教师等接口")
@RestController
@RequestMapping("/api/courses")
@Validated
public class CourseController {

    @Autowired
    private CourseService courseService;
    @Autowired
    private TeacherRepository teacherRepo;
    @Autowired
    private EnrollmentRepository enrollRepo;

    @Operation(summary = "查询所有课程", description = "返回课程列表，包含授课教师姓名和已选人数")
    @GetMapping
    public Result<List<CourseDTO>> getAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        List<CourseDTO> dtos = DtoConverter.toCourseDTOList(courses);
        for (CourseDTO dto : dtos) {
            if (dto.getTeacherId() != null) {
                Teacher t = teacherRepo.findById(dto.getTeacherId()).orElse(null);
                if (t != null) dto.setTeacherName(t.getName());
            }
            dto.setEnrolledCount(enrollRepo.countByCourseId(dto.getCourseId()));
        }
        return Result.success(dtos);
    }

    @Operation(summary = "按编号查询课程")
    @GetMapping("/{courseId}")
    public Result<CourseDTO> getCourseById(
            @Parameter(description = "课程编号") @PathVariable String courseId) {
        Course course = courseService.getCourseById(courseId);
        if (course == null) return Result.error(404, "课程不存在");
        CourseDTO dto = DtoConverter.toCourseDTO(course);
        dto.setEnrolledCount(enrollRepo.countByCourseId(courseId));
        return Result.success(dto);
    }

    @Operation(summary = "添加课程", description = "需要管理员权限")
    @PostMapping
    public Result<CourseDTO> addCourse(
            @Parameter(description = "课程编号") @RequestParam @NotBlank(message = "课程编号不能为空") String courseId,
            @Parameter(description = "课程名称") @RequestParam @NotBlank(message = "课程名称不能为空") String courseName,
            @Parameter(description = "学分") @RequestParam @NotNull(message = "学分不能为空") @Min(value = 1, message = "学分至少为1") Integer credit,
            @Parameter(description = "专业要求（可选）") @RequestParam(required = false) String specificRequirement,
            @Parameter(description = "容量上限（可选，默认50）") @RequestParam(required = false) Integer capacity) {
        Course course = new Course();
        course.setCourseId(courseId);
        course.setCourseName(courseName);
        course.setCredit(credit);
        course.setSpecificRequirement(specificRequirement);
        course.setIsArrangeTeacher(false);
        course.setCapacity(capacity != null ? capacity : 50);
        Course saved = courseService.addCourse(course);
        return Result.success("课程添加成功", DtoConverter.toCourseDTO(saved));
    }

    @Operation(summary = "删除课程", description = "需要管理员权限，会级联删除选课记录")
    @DeleteMapping("/{courseId}")
    public Result<String> deleteCourse(
            @Parameter(description = "课程编号") @PathVariable String courseId) {
        courseService.deleteCourse(courseId);
        return Result.success("课程删除成功");
    }

    @Operation(summary = "分配教师", description = "将课程分配给指定教师")
    @PutMapping("/assign")
    public Result<String> assignTeacher(
            @Parameter(description = "课程编号") @RequestParam @NotBlank(message = "课程编号不能为空") String courseId,
            @Parameter(description = "教师编号") @RequestParam @NotBlank(message = "教师编号不能为空") String teacherId) {
        courseService.assignTeacherToCourse(courseId, teacherId);
        return Result.success("课程分配教师成功");
    }

    @Operation(summary = "取消分配教师")
    @PutMapping("/unassign")
    public Result<String> unassignTeacher(
            @Parameter(description = "课程编号") @RequestParam @NotBlank(message = "课程编号不能为空") String courseId) {
        courseService.unassignTeacher(courseId);
        return Result.success("已取消教师分配");
    }
}