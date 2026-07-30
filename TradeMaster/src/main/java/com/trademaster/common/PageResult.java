package com.trademaster.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private List<T> records;
    private Long total;
    private Integer size;
    private Integer current;

    public static <T> PageResult<T> of(List<T> records, Long total, Integer size, Integer current) {
        return new PageResult<>(records, total, size, current);
    }
}
