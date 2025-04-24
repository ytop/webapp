package com.web.mra.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TableMetadataDTO {
    private String columnName;
    private String dataType;
    private boolean nullable;
    private boolean isPrimaryKey;
    private Integer maxLength;
}
