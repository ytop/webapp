package com.web.mra.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DetectionEmailMetaDTO {
    private Long autoId;
    private String userName;
    private String userDepartment;
    private Integer emailGroupBitmap;
    private String isActive;
}
