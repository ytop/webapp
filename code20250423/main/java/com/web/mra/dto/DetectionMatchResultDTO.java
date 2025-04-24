package com.web.mra.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO representing the result of a match comparison.
 */
@Data
@Builder
public class DetectionMatchResultDTO {
    private final boolean matches;
    private final String leftValue;
    private final Integer matchRule;
    private final String errorMessage;
}
