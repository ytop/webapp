package com.web.mra.service.detection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents the result of a detection component execution.
 */
@Getter
@RequiredArgsConstructor
public class DetectionResult {
    private final Integer detectionComponentId;
    private final Object result;
    private final Exception error;

    /**
     * Check if the detection was successful.
     *
     * @return true if successful, false if there was an error
     */
    public boolean isSuccess() {
        return error == null;
    }

    /**
     * Get error message if present.
     *
     * @return error message or null if no error
     */
    public String getErrorMessage() {
        return error != null ? error.getMessage() : null;
    }
}
