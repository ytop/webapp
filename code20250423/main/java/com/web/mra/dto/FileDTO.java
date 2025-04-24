package com.web.mra.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileDTO {
    private String name;
    private String type;
    private String path;
    private Long size;
    private Long modifiedDate;
}
