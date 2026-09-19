package com.example.course_system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "student")
public class Student {

    @Id
    @Column(name = "student_id")
    @NotBlank(message = "学号不能为空")
    private String studentId;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "专业不能为空")
    private String major;

    @Column(name = "password")
    private String password;

    public Student() {}

    public Student(String studentId, String name, String major) {
        this.studentId = studentId;
        this.name = name;
        this.major = major;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}