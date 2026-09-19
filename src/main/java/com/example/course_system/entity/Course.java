package com.example.course_system.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "course")
public class Course implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "course_id", length = 20)
    private String courseId;

    @Column(name = "course_name", nullable = false)
    private String courseName;

    private Integer credit;

    @Column(name = "specific_requirement")
    private String specificRequirement;

    @Column(name = "is_arrange_teacher")
    private Boolean isArrangeTeacher = false;

    @Column(name = "teacher_id")
    private String teacherId;

    // 课程容量上限，不能为空，默认 50
    @Column(name = "capacity", nullable = false)
    private Integer capacity = 50;

    public Course() {}

    public Course(String courseId, String courseName, Integer credit, String specificRequirement, Boolean isArrangeTeacher) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.credit = credit;
        this.specificRequirement = specificRequirement;
        this.isArrangeTeacher = isArrangeTeacher;
        this.capacity = 50;
    }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public Integer getCredit() { return credit; }
    public void setCredit(Integer credit) { this.credit = credit; }
    public String getSpecificRequirement() { return specificRequirement; }
    public void setSpecificRequirement(String specificRequirement) { this.specificRequirement = specificRequirement; }
    public Boolean getIsArrangeTeacher() { return isArrangeTeacher; }
    public void setIsArrangeTeacher(Boolean isArrangeTeacher) { this.isArrangeTeacher = isArrangeTeacher; }
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
}