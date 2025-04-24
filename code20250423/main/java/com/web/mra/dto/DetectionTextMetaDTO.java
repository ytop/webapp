package com.web.mra.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DetectionTextMetaDTO {
    private Long autoId;
    private Integer detectionComponentId;
    private String textFileName;
    private String textRegexJson;
}
