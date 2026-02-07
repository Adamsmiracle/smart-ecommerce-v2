package com.miracle.smart_ecommerce_api_v1.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspect for logging service layer method calls.
 * Implements @Before, @After, @AfterReturning, @AfterThrowing, and @Around advice.
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Pointcut for all service methods
     */
    @Pointcut("execution(* com.miracle.smart_ecommerce.service.impl.*.*(..))")
    public void serviceLayerMethods() {}

    /**
     * Pointcut for all repository methods
     */
    @Pointcut("execution(* com.miracle.smart_ecommerce.repository.impl.*.*(..))")
    public void repositoryLayerMethods() {}

    /**
     * Pointcut for all controller methods
     */
    @Pointcut("execution(* com.miracle.smart_ecommerce.controller.*.*(..))")
    public void controllerLayerMethods() {}

    /**
     * Before advice - logs method entry with arguments
     */
    @Before("serviceLayerMethods()")
    public void logMethodEntry(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        log.debug("Entering method: {} with arguments: {}", methodName, Arrays.toString(args));
    }

    /**
     * After advice - logs method exit
     */
    @After("serviceLayerMethods()")
    public void logMethodExit(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        log.debug("Exiting method: {}", methodName);
    }

    /**
     * AfterReturning advice - logs method result
     */
    @AfterReturning(pointcut = "serviceLayerMethods()", returning = "result")
    public void logMethodReturn(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().toShortString();
        log.debug("Method {} returned: {}", methodName,
                result != null ? result.getClass().getSimpleName() : "null");
    }

    /**
     * AfterThrowing advice - logs exceptions
     */
    @AfterThrowing(pointcut = "serviceLayerMethods()", throwing = "exception")
    public void logMethodException(JoinPoint joinPoint, Throwable exception) {
        String methodName = joinPoint.getSignature().toShortString();
        log.error("Exception in method {}: {} - {}",
                methodName,
                exception.getClass().getSimpleName(),
                exception.getMessage());
    }

    /**
     * Around advice - logs execution time for repository methods
     */
    @Around("repositoryLayerMethods()")
    public Object logRepositoryExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            if (executionTime > 100) {
                log.warn("Slow query detected - Method {} took {} ms", methodName, executionTime);
            } else {
                log.debug("Method {} executed in {} ms", methodName, executionTime);
            }

            return result;
        } catch (Throwable throwable) {
            long endTime = System.currentTimeMillis();
            log.error("Method {} failed after {} ms with exception: {}",
                    methodName,
                    (endTime - startTime),
                    throwable.getMessage());
            throw throwable;
        }
    }
}

