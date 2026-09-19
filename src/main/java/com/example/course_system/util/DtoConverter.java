package com.example.course_system.util;

import com.example.course_system.dto.*;
import com.example.course_system.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DtoConverter {

    // ===== Course =====
    public static CourseDTO toCourseDTO(Course course) {
        if (course == null) return null;
        return new CourseDTO(
                course.getCourseId(),
                course.getCourseName(),
                course.getCredit(),
                course.getSpecificRequirement(),
                course.getIsArrangeTeacher(),
                course.getTeacherId(),
                null,
                course.getCapacity(),
                null
        );
    }

    public static List<CourseDTO> toCourseDTOList(List<Course> courses) {
        return courses.stream().map(DtoConverter::toCourseDTO).collect(Collectors.toList());
    }

    // ===== Student =====
    public static StudentDTO toStudentDTO(Student student) {
        if (student == null) return null;
        return new StudentDTO(student.getStudentId(), student.getName(), student.getMajor());
    }

    public static List<StudentDTO> toStudentDTOList(List<Student> students) {
        return students.stream().map(DtoConverter::toStudentDTO).collect(Collectors.toList());
    }

    // ===== Teacher =====
    public static TeacherDTO toTeacherDTO(Teacher teacher) {
        if (teacher == null) return null;
        return new TeacherDTO(teacher.getTeacherId(), teacher.getName());
    }

    public static List<TeacherDTO> toTeacherDTOList(List<Teacher> teachers) {
        return teachers.stream().map(DtoConverter::toTeacherDTO).collect(Collectors.toList());
    }

    // ===== Enrollment =====
    public static EnrollmentDTO toEnrollmentDTO(Enrollment e) {
        if (e == null) return null;
        return new EnrollmentDTO(e.getId(), e.getStudentId(), e.getCourseId(), e.getGrade());
    }

    public static List<EnrollmentDTO> toEnrollmentDTOList(List<Enrollment> enrollments) {
        return enrollments.stream().map(DtoConverter::toEnrollmentDTO).collect(Collectors.toList());
    }
}