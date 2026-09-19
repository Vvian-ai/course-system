package com.example.course_system;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Spring 上下文加载测试
 * 注意：此测试会连接真实 MySQL 和 Redis（因为项目没有专门的 test profile），
 * 运行前请确保 MySQL 和 Redis 已启动，且 application-dev.properties 配置正确。
 */
@SpringBootTest
class CourseSystemApplicationTests {

	@Test
	void contextLoads() {
		// 只验证 Spring 上下文能否加载

	}

}