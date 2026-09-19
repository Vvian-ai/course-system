package com.example.course_system.controller;

import com.example.course_system.common.Result;
import com.example.course_system.entity.Student;
import com.example.course_system.entity.Teacher;
import com.example.course_system.repository.StudentRepository;
import com.example.course_system.repository.TeacherRepository;
import com.example.course_system.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "登录认证", description = "用户登录获取 JWT Token")
@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private StudentRepository studentRepo;
    @Autowired
    private TeacherRepository teacherRepo;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Operation(summary = "用户登录", description = "输入用户名、密码、角色，返回 JWT Token")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @Parameter(description = "用户名（学号/教师编号）") @RequestParam String username,
            @Parameter(description = "密码") @RequestParam String password,
            @Parameter(description = "角色（student/teacher/admin）") @RequestParam String role,
            HttpServletResponse response) {

        String userId = null;
        String userRole = null;

        if ("student".equals(role)) {
            Student student = studentRepo.findById(username).orElse(null);
            if (student == null) {
                return Result.error(401, "学号不存在");
            }
            if (student.getPassword() == null || !passwordEncoder.matches(password, student.getPassword())) {
                return Result.error(401, "密码错误");
            }
            userId = student.getStudentId();
            userRole = "student";

        } else if ("teacher".equals(role)) {
            Teacher teacher = teacherRepo.findById(username).orElse(null);
            if (teacher == null) {
                return Result.error(401, "教师编号不存在");
            }
            if (teacher.getPassword() == null || !passwordEncoder.matches(password, teacher.getPassword())) {
                return Result.error(401, "密码错误");
            }
            userId = teacher.getTeacherId();
            userRole = "teacher";

        } else if ("admin".equals(role)) {
            // Admin password also uses BCrypt comparison
            if (!adminUsername.equals(username) || !passwordEncoder.matches(password, adminPassword)) {
                return Result.error(401, "管理员账号或密码错误");
            }
            userId = "admin";
            userRole = "admin";

        } else {
            return Result.error(400, "无效的角色");
        }

        String token = jwtUtil.generateToken(userId, userRole);

        ResponseCookie cookie = ResponseCookie.from("token", token)
                .path("/")
                .httpOnly(true)
                .sameSite("Lax")
                .maxAge(Duration.ofHours(24))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("username", userId);
        data.put("role", userRole);

        return Result.success(data);
    }

    @Operation(summary = "退出登录", description = "清除服务端 Cookie")
    @PostMapping("/logout")
    public Result<String> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("token", "")
                .path("/")
                .httpOnly(true)
                .sameSite("Lax")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return Result.success("已退出登录");
    }
}