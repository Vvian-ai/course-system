package com.example.course_system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "teacher")
public class Teacher {

    @Id
    @Column(name = "teacher_id")
    @NotBlank(message = "教师编号不能为空")
    private String teacherId;

    @NotBlank(message = "教师姓名不能为空")
    private String name;

    @Column(name = "password")
    private String password;

    public Teacher() {}

    public Teacher(String teacherId, String name) {
        this.teacherId = teacherId;
        this.name = name;
    }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}