-- ========================================
-- 校园课程管理系统 - 数据库表结构
-- ========================================

CREATE TABLE IF NOT EXISTS course (
                                      course_id VARCHAR(20) NOT NULL,
    course_name VARCHAR(255) NOT NULL,
    credit INTEGER,
    specific_requirement VARCHAR(255),
    is_arrange_teacher BIT,
    teacher_id VARCHAR(20),
    capacity INTEGER NOT NULL,
    PRIMARY KEY (course_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS student (
                                       student_id VARCHAR(20) NOT NULL,
    name VARCHAR(255),
    major VARCHAR(255),
    password VARCHAR(255),
    PRIMARY KEY (student_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS teacher (
                                       teacher_id VARCHAR(20) NOT NULL,
    name VARCHAR(255),
    password VARCHAR(255),
    PRIMARY KEY (teacher_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS enrollment (
                                          id INTEGER NOT NULL AUTO_INCREMENT,
                                          student_id VARCHAR(255),
    course_id VARCHAR(255),
    grade INTEGER,
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_course (student_id, course_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;