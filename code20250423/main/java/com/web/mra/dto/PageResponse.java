package com.web.mra.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageResponse<T> {
    private List<T> content;
    private long total;
    private int page;
    private int size;
}
