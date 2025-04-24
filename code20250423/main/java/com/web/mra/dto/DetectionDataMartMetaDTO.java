package com.web.mra.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DetectionDataMartMetaDTO {
    private Long autoId;
    private Integer detectionComponentId;
    private String sqlTemplate;
    private String sqlParams;
}
