package com.web.mra.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DetectionExcelMetaDTO {
    private Long autoId;
    private Integer detectionComponentId;
    private String excelFileName;
    private String sheetName;
    private String cellPositions;
    private String cellParams;
}
