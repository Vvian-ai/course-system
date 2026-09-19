package com.example.course_system.controller;

import com.example.course_system.entity.*;
import com.example.course_system.service.CourseService;
import com.example.course_system.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class PageController {

    @Autowired
    private CourseService courseService;
    @Autowired
    private StudentRepository studentRepo;
    @Autowired
    private TeacherRepository teacherRepo;
    @Autowired
    private EnrollmentRepository enrollRepo;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/student")
    public String studentPage(Model model) {
        List<Course> courses = courseService.getAllCourses();
        model.addAttribute("courses", courses);
        return "student-page";
    }

    @GetMapping("/teacher")
    public String teacherPage(Model model) {
        List<Course> allCourses = courseService.getAllCourses();
        model.addAttribute("allCourses", allCourses);
        return "teacher-page";
    }

    @GetMapping("/admin")
    public String adminPage(Model model) {
        List<Student> students = studentRepo.findAll();
        List<Teacher> teachers = teacherRepo.findAll();
        List<Course> courses = courseService.getAllCourses();
        List<Enrollment> enrollments = enrollRepo.findAll();
        model.addAttribute("students", students);
        model.addAttribute("teachers", teachers);
        model.addAttribute("courses", courses);
        model.addAttribute("enrollments", enrollments);
        return "admin";
    }

    @GetMapping("/view/courses")
    public String viewCourses(Model model) {
        List<Course> courses = courseService.getAllCourses();
        Map<String, Long> countMap = new HashMap<>();
        for (Course c : courses) {
            long count = enrollRepo.countByCourseId(c.getCourseId());
            countMap.put(c.getCourseId(), count);
        }
        model.addAttribute("courses", courses);
        model.addAttribute("countMap", countMap);
        return "course-list";
    }

    @GetMapping("/view/student/{studentId}")
    public String viewStudent(@PathVariable String studentId, Model model,
                              HttpServletRequest request, HttpServletResponse response) {
        String currentUser = (String) request.getAttribute("username");
        String currentRole = (String) request.getAttribute("role");

        // 学生只能看自己
        if ("student".equals(currentRole) && !studentId.equals(currentUser)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            model.addAttribute("error", "您无权查看他人信息");
            return "error";
        }

        Student student = studentRepo.findById(studentId).orElse(null);
        if (student == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            model.addAttribute("error", "学生不存在");
            return "error";
        }
        List<Enrollment> enrollments = enrollRepo.findByStudentId(studentId);
        model.addAttribute("student", student);
        model.addAttribute("enrollments", enrollments);
        return "student-detail";
    }
}