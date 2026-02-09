package com.miracle.smart_ecommerce_api_v1.exception;

/**
 * Exception thrown for service unavailability.
 */
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message) {
        super(message);
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ServiceUnavailableException forService(String serviceName) {
        return new ServiceUnavailableException("Service temporarily unavailable: " + serviceName);
    }

    public static ServiceUnavailableException forDatabaseConnection() {
        return new ServiceUnavailableException("Database connection unavailable");
    }

    public static ServiceUnavailableException forExternalApi(String apiName) {
        return new ServiceUnavailableException("External API unavailable: " + apiName);
    }
}

