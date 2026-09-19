package com.example.course_system.interceptor;

import com.example.course_system.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 规则：哪些路径 + 方法组合，只有特定角色能访问
     * 格式：{路径前缀, 方法（空表示所有）, 允许的角色列表}
     */
    private static final List<Rule> RULES = Arrays.asList(
            // 课程管理：只有管理员能增删改
            new Rule("/api/courses", "POST", "admin"),
            new Rule("/api/courses", "DELETE", "admin"),
            new Rule("/api/courses", "PUT", "admin"),
            // 学生管理：只有管理员能增删
            new Rule("/api/students", "POST", "admin"),
            new Rule("/api/students", "DELETE", "admin"),
            // 教师管理：只有管理员能增删
            new Rule("/api/teachers", "POST", "admin"),
            new Rule("/api/teachers", "DELETE", "admin"),
            // 成绩录入：只有教师
            new Rule("/api/enrollments/grade", "", "teacher"),
            // 选课名单：只有教师
            new Rule("/api/enrollments/byCourse", "", "teacher")
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        boolean isApi = uri.startsWith("/api/");

        // 取 Token：优先请求头，其次 Cookie
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if ("token".equals(c.getName())) {
                        token = "Bearer " + c.getValue();
                        break;
                    }
                }
            }
        }

        // 未登录
        if (token == null || token.isEmpty()) {
            if (isApi) return denyJson(response, 401, "未登录，请先登录");
            response.sendRedirect("/login");
            return false;
        }

        if (!token.startsWith("Bearer ")) {
            if (isApi) return denyJson(response, 401, "Token 格式错误");
            response.sendRedirect("/login");
            return false;
        }

        token = token.substring(7);

        if (!jwtUtil.validateToken(token)) {
            if (isApi) return denyJson(response, 401, "Token 已过期或无效");
            response.sendRedirect("/login");
            return false;
        }

        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        request.setAttribute("username", username);
        request.setAttribute("role", role);

        String method = request.getMethod();

        // 页面权限：只有管理员能访问 /admin
        if (uri.startsWith("/admin") && !"admin".equals(role)) {
            response.sendRedirect("/login");
            return false;
        }

        // 管理员有所有权限
        if ("admin".equals(role)) {
            return true;
        }

        // 通用规则检查
        for (Rule rule : RULES) {
            if (uri.startsWith(rule.pathPrefix)
                    && (rule.method.isEmpty() || rule.method.equals(method))) {
                if (!rule.allowedRoles.contains(role)) {
                    return denyJson(response, 403, "权限不足");
                }
            }
        }

        // 学生只能操作自己的选课记录
        if ("student".equals(role) && uri.startsWith("/api/enrollments")) {
            String studentId = request.getParameter("studentId");
            if (studentId != null && !studentId.equals(username)) {
                return denyJson(response, 403, "您只能操作自己的选课记录");
            }
        }

        return true;
    }

    private boolean denyJson(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + code + ",\"message\":\"" + message + "\"}");
        return false;
    }

    /**
     * 权限规则
     */
    private static class Rule {
        String pathPrefix;
        String method;
        List<String> allowedRoles;

        Rule(String pathPrefix, String method, String... roles) {
            this.pathPrefix = pathPrefix;
            this.method = method;
            this.allowedRoles = Arrays.asList(roles);
        }
    }
}