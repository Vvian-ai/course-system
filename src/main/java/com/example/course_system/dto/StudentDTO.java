package com.example.course_system.dto;

import java.util.List;

public class StudentDTO {

    private String studentId;
    private String name;
    private String major;
    private List<String> enrolledCourseNames;  // 已选课程名称列表

    public StudentDTO() {}

    public StudentDTO(String studentId, String name, String major) {
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
    public List<String> getEnrolledCourseNames() { return enrolledCourseNames; }
    public void setEnrolledCourseNames(List<String> enrolledCourseNames) { this.enrolledCourseNames = enrolledCourseNames; }
}