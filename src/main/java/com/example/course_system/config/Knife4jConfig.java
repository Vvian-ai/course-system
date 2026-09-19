package com.example.course_system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("校园课程管理系统 API 文档")
                        .description("提供课程、学生、教师、选课、成绩等核心业务的 RESTful 接口")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("徐威龙")
                                .email("xxx@example.com")));
    }
}