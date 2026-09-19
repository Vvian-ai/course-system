package com.example.course_system.exception;

/**
 * 业务异常：用于抛出可预期的业务错误（如"选课已达上限""课程不存在"），
 * 全局异常处理器会把它转成 400 + 业务提示返回给前端。
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}