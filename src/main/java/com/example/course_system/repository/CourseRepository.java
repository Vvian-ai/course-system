package com.example.course_system.repository;

import com.example.course_system.entity.Course;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, String> {

    /**
     * 带悲观写锁的查询，等价于 SELECT ... FOR UPDATE。
     *
     * 用于选课时的容量校验：给这一行课程数据加上写锁，
     * 让同一门课的并发选课请求排队执行，避免多个请求同时读到相同的已选人数。
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Course c WHERE c.courseId = :id")
    Optional<Course> findByIdForUpdate(@Param("id") String id);
}