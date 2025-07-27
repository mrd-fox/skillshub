package com.simplon_project.skillhub.skillhub.common.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // Pointcut in all methods publics in  package storage.adapter
    @Pointcut("execution(public * com.simplon_project.skillhub.skillhub.storage.adapter..*(..))")
    public void storageAdapterMethods() {
    }

    @Before("storageAdapterMethods()")
    public void logBefore(JoinPoint joinPoint) {
        logger.info("Call method: " + joinPoint.getSignature().toShortString());
    }

    @AfterReturning(pointcut = "storageAdapterMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        logger.info("Method finished: " + joinPoint.getSignature().toShortString() + " result: " + result);
    }

    @AfterThrowing(pointcut = "storageAdapterMethods()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        logger.error("Exception in method: " + joinPoint.getSignature().toShortString(), ex);
    }
}
