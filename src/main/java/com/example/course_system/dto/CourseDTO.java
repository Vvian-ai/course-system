package com.example.course_system.dto;

public class CourseDTO {

    private String courseId;
    private String courseName;
    private Integer credit;
    private String specificRequirement;
    private Boolean isArrangeTeacher;
    private String teacherId;
    private String teacherName;
    private Integer capacity;
    private Long enrolledCount;  // 已选人数

    public CourseDTO() {}

    public CourseDTO(String courseId, String courseName, Integer credit,
                     String specificRequirement, Boolean isArrangeTeacher,
                     String teacherId, String teacherName, Integer capacity, Long enrolledCount) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.credit = credit;
        this.specificRequirement = specificRequirement;
        this.isArrangeTeacher = isArrangeTeacher;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.capacity = capacity;
        this.enrolledCount = enrolledCount;
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
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public Long getEnrolledCount() { return enrolledCount; }
    public void setEnrolledCount(Long enrolledCount) { this.enrolledCount = enrolledCount; }
}