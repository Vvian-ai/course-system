package com.example.course_system.dto;

public class TeacherDTO {

    private String teacherId;
    private String name;

    public TeacherDTO() {}

    public TeacherDTO(String teacherId, String name) {
        this.teacherId = teacherId;
        this.name = name;
    }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}