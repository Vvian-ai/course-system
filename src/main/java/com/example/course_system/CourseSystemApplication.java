package com.example.course_system;

import com.example.course_system.entity.*;
import com.example.course_system.repository.StudentRepository;
import com.example.course_system.repository.TeacherRepository;
import com.example.course_system.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableCaching
public class CourseSystemApplication implements CommandLineRunner {

	@Autowired
	private CourseService courseService;
	@Autowired
	private StudentRepository studentRepo;
	@Autowired
	private TeacherRepository teacherRepo;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(CourseSystemApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// 初始化数据（仅当课程表为空时执行）
		if (courseService.getAllCourses().isEmpty()) {
			System.out.println(">>> 初始化数据：插入课程、学生、教师...");

			Course c1 = new Course("001", "高等数学", 5, null, false);
			Course c2 = new Course("002", "数据结构", 4, "需掌握编程语言基础", false);
			Course c3 = new Course("003", "操作系统", 4, "了解计算机硬件知识", false);
			courseService.addCourse(c1);
			courseService.addCourse(c2);
			courseService.addCourse(c3);

			Student s1 = new Student("1001", "张三", "计算机科学");
			Student s2 = new Student("1002", "李四", "计算机科学");
			s1.setPassword(passwordEncoder.encode("123456"));
			s2.setPassword(passwordEncoder.encode("123456"));
			studentRepo.save(s1);
			studentRepo.save(s2);

			Teacher t1 = new Teacher("2001", "王五");
			Teacher t2 = new Teacher("2002", "赵六");
			t1.setPassword(passwordEncoder.encode("123456"));
			t2.setPassword(passwordEncoder.encode("123456"));
			teacherRepo.save(t1);
			teacherRepo.save(t2);

			System.out.println("✅ 初始数据插入成功！");
		}
	}
}
