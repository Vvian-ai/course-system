package com.example.course_system.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "enrollment",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_student_course",
                columnNames = {"student_id", "course_id"}
        ))
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "student_id")
    private String studentId;

    @Column(name = "course_id")
    private String courseId;

    // 成绩：null 表示未录入
    private Integer grade;

    public Enrollment() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public Integer getGrade() { return grade; }
    public void setGrade(Integer grade) { this.grade = grade; }
}