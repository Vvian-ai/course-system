package com.example.course_system.controller;

import com.example.course_system.common.Result;
import com.example.course_system.dto.TeacherDTO;
import com.example.course_system.entity.Course;
import com.example.course_system.entity.Teacher;
import com.example.course_system.repository.CourseRepository;
import com.example.course_system.repository.TeacherRepository;
import com.example.course_system.util.DtoConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "教师管理", description = "教师信息的增删改查接口")
@RestController
@RequestMapping("/api/teachers")
@Validated
public class TeacherController {

    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Operation(summary = "查询所有教师")
    @GetMapping
    public Result<List<TeacherDTO>> getAllTeachers() {
        return Result.success(DtoConverter.toTeacherDTOList(teacherRepository.findAll()));
    }

    @Operation(summary = "按编号查询教师")
    @GetMapping("/{teacherId}")
    public Result<TeacherDTO> getTeacherById(
            @Parameter(description = "教师编号") @PathVariable String teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId).orElse(null);
        if (teacher == null) return Result.error(404, "教师不存在");
        return Result.success(DtoConverter.toTeacherDTO(teacher));
    }

    @Operation(summary = "添加教师", description = "需要管理员权限，密码可选，默认123456")
    @PostMapping
    public Result<TeacherDTO> addTeacher(
            @Parameter(description = "教师编号") @RequestParam @NotBlank(message = "教师编号不能为空") String teacherId,
            @Parameter(description = "教师姓名") @RequestParam @NotBlank(message = "教师姓名不能为空") String name,
            @Parameter(description = "密码（可选，默认123456）") @RequestParam(required = false) String password) {
        Teacher teacher = new Teacher();
        teacher.setTeacherId(teacherId);
        teacher.setName(name);
        String rawPassword = (password != null && !password.isEmpty()) ? password : "123456";
        teacher.setPassword(passwordEncoder.encode(rawPassword));
        Teacher saved = teacherRepository.save(teacher);
        return Result.success("教师添加成功", DtoConverter.toTeacherDTO(saved));
    }

    @Operation(summary = "删除教师", description = "需要管理员权限，会解除该教师的授课关系")
    @DeleteMapping("/{teacherId}")
    public Result<String> deleteTeacher(
            @Parameter(description = "教师编号") @PathVariable String teacherId) {
        if (!teacherRepository.existsById(teacherId)) {
            return Result.error(404, "教师不存在");
        }

        List<Course> courses = courseRepository.findAll();
        for (Course course : courses) {
            if (teacherId.equals(course.getTeacherId())) {
                course.setTeacherId(null);
                course.setIsArrangeTeacher(false);
                courseRepository.save(course);
            }
        }

        teacherRepository.deleteById(teacherId);

        if (cacheManager.getCache("courses") != null) {
            cacheManager.getCache("courses").clear();
        }

        return Result.success("教师删除成功");
    }
}