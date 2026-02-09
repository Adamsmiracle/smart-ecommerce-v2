package com.miracle.smart_ecommerce_api_v1.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Aspect for security monitoring and access control logging.
 * Monitors authentication and authorization events.
 */
@Aspect
@Component
@Slf4j
public class SecurityAspect {

    /**
     * Pointcut for methods annotated with Spring Security annotations
     */
    @Pointcut("@annotation(org.springframework.security.access.prepost.PreAuthorize) || " +
              "@annotation(org.springframework.security.access.annotation.Secured)")
    public void securedMethods() {}

    /**
     * Pointcut for controller methods
     */
    @Pointcut("execution(* com.miracle.smart_ecommerce_api_v1.controller.*.*(..))")
    public void controllerMethods() {}

    /**
     * Log secured method access attempts
     */
    @Before("securedMethods()")
    public void logSecuredAccess(JoinPoint joinPoint) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String methodName = joinPoint.getSignature().toShortString();

        if (authentication != null && authentication.isAuthenticated()) {
            log.info("SECURITY - User '{}' accessing secured method: {}",
                    authentication.getName(), methodName);
        } else {
            log.warn("SECURITY - Unauthenticated access attempt to secured method: {}", methodName);
        }
    }

    /**
     * Log access denied exceptions
     */
    @AfterThrowing(pointcut = "securedMethods() || controllerMethods()", throwing = "exception")
    public void logAccessDenied(JoinPoint joinPoint, AccessDeniedException exception) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String methodName = joinPoint.getSignature().toShortString();
        String username = authentication != null ? authentication.getName() : "anonymous";

        log.error("ACCESS DENIED - User '{}' denied access to method: {} | Reason: {}",
                 username, methodName, exception.getMessage());
    }

    /**
     * Log authentication failures
     */
    @AfterThrowing(pointcut = "controllerMethods()", throwing = "exception")
    public void logAuthenticationFailure(JoinPoint joinPoint,
                                        org.springframework.security.core.AuthenticationException exception) {
        String methodName = joinPoint.getSignature().toShortString();

        log.error("AUTHENTICATION FAILED - Method: {} | Reason: {}",
                 methodName, exception.getMessage());
    }
}

