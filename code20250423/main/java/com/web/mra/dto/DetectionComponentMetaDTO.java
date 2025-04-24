package com.web.mra.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DetectionComponentMetaDTO {
    private Long autoId;
    private Integer detectionComponentId;
    private String componentLabels;
    private Integer componentDataTypes;
    private Integer detectionComponentSource;
    private String detectionComponentDescription;
    private String isActive;
}
