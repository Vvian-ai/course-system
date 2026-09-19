package com.example.course_system.aspect;

import com.example.course_system.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class LogAspect {

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    @Pointcut("execution(* com.example.course_system.controller..*(..))")
    public void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String method = "";
        String uri = "";
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            method = request.getMethod();
            uri = request.getRequestURI();
        }

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // 用参数名匹配，对包含 password 的参数脱敏
        String argsStr = formatArgs(joinPoint, args);

        log.info(">>> [请求] {} {} | 方法: {}.{} | 参数: {}",
                method, uri, className, methodName, argsStr);

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            long cost = System.currentTimeMillis() - startTime;
            log.error("<<< [异常] {} {} | 方法: {}.{} | 耗时: {}ms | 异常: {}",
                    method, uri, className, methodName, cost, e.getMessage());
            throw e;
        }

        long cost = System.currentTimeMillis() - startTime;
        String resultStr = formatResult(result);

        log.info("<<< [响应] {} {} | 方法: {}.{} | 耗时: {}ms | 结果: {}",
                method, uri, className, methodName, cost, resultStr);

        return result;
    }

    /**
     * 参数格式化：按参数名匹配，包含 password 的参数脱敏
     */
    private String formatArgs(ProceedingJoinPoint joinPoint, Object[] args) {
        if (args == null || args.length == 0) return "[]";

        // 获取参数名
        String[] paramNames = null;
        try {
            MethodSignature sig = (MethodSignature) joinPoint.getSignature();
            paramNames = sig.getParameterNames();
        } catch (Exception ignored) {}

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            Object arg = args[i];

            // HttpServletRequest / HttpServletResponse 跳过
            if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse) {
                sb.append(arg.getClass().getSimpleName());
                continue;
            }

            // 按参数名匹配：包含 password 的一律脱敏
            if (paramNames != null && i < paramNames.length && paramNames[i] != null
                    && paramNames[i].toLowerCase().contains("password")) {
                sb.append("***");
                continue;
            }

            sb.append(arg != null ? arg.toString() : "null");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 结果格式化：Result 类型只打印 code 和 message，data 脱敏
     */
    private String formatResult(Object result) {
        if (result == null) return "null";

        if (result instanceof Result) {
            Result<?> r = (Result<?>) result;
            return "Result{code=" + r.getCode() + ", message='" + r.getMessage() + "', data=***}";
        }

        String str = result.toString();
        if (str.length() > 200) {
            str = str.substring(0, 200) + "...";
        }
        return str;
    }
}