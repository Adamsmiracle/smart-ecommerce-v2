package com.miracle.smart_ecommerce_api_v1.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Aspect for performance monitoring.
 * Tracks method execution time and logs slow operations.
 */
@Aspect
@Component
public class PerformanceAspect {

    private static final Logger log = LoggerFactory.getLogger(PerformanceAspect.class);

    // Threshold for slow operations (in milliseconds)
    private static final long SLOW_THRESHOLD_MS = 500;
    private static final long VERY_SLOW_THRESHOLD_MS = 1000;

    /**
     * Pointcut for all service layer methods
     */
    @Pointcut("execution(* com.miracle.smart_ecommerce.service.impl.*.*(..))")
    public void serviceLayerMethods() {}

    /**
     * Pointcut for all controller methods
     */
    @Pointcut("execution(* com.miracle.smart_ecommerce.controller.*.*(..))")
    public void controllerMethods() {}

    /**
     * Around advice - monitors service layer performance
     */
    @Around("serviceLayerMethods()")
    public Object monitorServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        return measureAndLog(joinPoint, "Service");
    }

    /**
     * Around advice - monitors controller performance
     */
    @Around("controllerMethods()")
    public Object monitorControllerPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        return measureAndLog(joinPoint, "Controller");
    }

    private Object measureAndLog(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            logPerformance(layer, methodName, executionTime);

            return result;
        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("[{}] {} failed after {} ms: {}",
                    layer, methodName, executionTime, throwable.getMessage());
            throw throwable;
        }
    }

    private void logPerformance(String layer, String methodName, long executionTime) {
        if (executionTime >= VERY_SLOW_THRESHOLD_MS) {
            log.warn("[{}] VERY SLOW: {} took {} ms", layer, methodName, executionTime);
        } else if (executionTime >= SLOW_THRESHOLD_MS) {
            log.warn("[{}] SLOW: {} took {} ms", layer, methodName, executionTime);
        } else {
            log.debug("[{}] {} completed in {} ms", layer, methodName, executionTime);
        }
    }
}

