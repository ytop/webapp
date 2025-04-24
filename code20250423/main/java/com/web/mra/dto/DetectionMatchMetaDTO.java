package com.web.mra.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DetectionMatchMetaDTO {
    private Long autoId;
    private Integer leftdetectionComponentId;
    private Integer rightdetectionComponentId;
    private Integer matchRule;
    private Double matchThreshold;
    private String isActive;
}
