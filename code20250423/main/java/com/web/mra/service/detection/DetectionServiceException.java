// DetectionServiceException.java
package com.web.mra.service.detection;

public class DetectionServiceException extends RuntimeException {
    public DetectionServiceException(String message) {
        super(message);
    }

    public DetectionServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

