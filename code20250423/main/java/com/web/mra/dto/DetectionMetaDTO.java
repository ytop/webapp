package com.web.mra.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DetectionMetaDTO {
    private Long autoId;
    private Integer detectionId;
    private String detectionDept;
    private String detectionDesc;
}
