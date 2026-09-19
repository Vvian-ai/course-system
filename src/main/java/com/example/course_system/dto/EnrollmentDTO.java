package com.example.course_system.dto;

public class EnrollmentDTO {

    private Integer id;
    private String studentId;
    private String courseId;
    private Integer grade;

    public EnrollmentDTO() {}

    public EnrollmentDTO(Integer id, String studentId, String courseId, Integer grade) {
        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.grade = grade;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public Integer getGrade() { return grade; }
    public void setGrade(Integer grade) { this.grade = grade; }
}