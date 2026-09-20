-- ============================================================
-- 选课并发压测的数据准备与清理脚本
-- 配套文档：docs/压测记录.md
-- ============================================================

USE course_system;

-- ============================================================
-- 一、准备数据
-- ============================================================

SET SESSION cte_max_recursion_depth = 1000;

-- 1. 生成 200 个测试学生：T001 ~ T200，密码统一为 123456
--    密码复用现有学生的 BCrypt 哈希，避免重复加密
INSERT INTO student (student_id, name, major, password)
SELECT CONCAT('T', LPAD(n, 3, '0')),
       CONCAT('测试学生', n),
       '计算机科学',
       (SELECT password FROM student WHERE student_id = '1001')
FROM (
    WITH RECURSIVE seq(n) AS (
        SELECT 1
        UNION ALL
        SELECT n + 1 FROM seq WHERE n < 200
    )
    SELECT n FROM seq
) AS s;

-- 2. 生成一门压测专用课程，容量 50
INSERT INTO course (course_id, course_name, credit, specific_requirement,
                    is_arrange_teacher, teacher_id, capacity)
VALUES ('TEST01', '压测专用课程', 2, NULL, b'0', NULL, 50);

-- 3. 验证
SELECT COUNT(*) AS '测试学生数（应为 200）' FROM student WHERE student_id LIKE 'T%';
SELECT course_id, course_name, capacity FROM course WHERE course_id = 'TEST01';


-- ============================================================
-- 二、每轮压测前重置（不用重新造数据）
-- ============================================================
-- DELETE FROM enrollment WHERE course_id = 'TEST01';


-- ============================================================
-- 三、压测后统计
-- ============================================================
-- SELECT COUNT(*) AS '实际选课人数' FROM enrollment WHERE course_id = 'TEST01';
-- SELECT COUNT(*) AS '重复选课记录数' FROM (
--     SELECT student_id FROM enrollment WHERE course_id = 'TEST01'
--     GROUP BY student_id HAVING COUNT(*) > 1
-- ) t;


-- ============================================================
-- 四、清理（压测全部完成后执行）
-- ============================================================
-- DELETE FROM enrollment WHERE course_id = 'TEST01';
-- DELETE FROM course WHERE course_id = 'TEST01';
-- DELETE FROM student WHERE student_id LIKE 'T%';
