package com.cardmonix.cardmonix.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaginationUtils implements Serializable {
    private List<?> content;
    private Integer pageNo;
    private Integer pageSize;
    private Long totalElements;
    private Integer totalPage;
    private Boolean isLast;



}