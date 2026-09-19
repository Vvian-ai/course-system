package com.example.course_system.repository;

import com.example.course_system.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {
    List<Enrollment> findByStudentId(String studentId);
    List<Enrollment> findByCourseId(String courseId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Enrollment e WHERE e.studentId = ?1 AND e.courseId = ?2")
    void deleteByStudentIdAndCourseId(String studentId, String courseId);

    long countByCourseId(String courseId);
}