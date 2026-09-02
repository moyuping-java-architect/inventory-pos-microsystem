package com.psi.common.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;
    private String message;
    private List<T> list;
    private long total;
    private int pageNum;
    private int pageSize;
    private int pages;
    private long timestamp;

    public PageResult() {
        this.timestamp = System.currentTimeMillis();
    }

    public PageResult(int code, String message, List<T> list, long total, int pageNum, int pageSize) {
        this.code = code;
        this.message = message;
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = pageSize > 0 ? (int) Math.ceil((double) total / pageSize) : 0;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> PageResult<T> success(List<T> list, long total, int pageNum, int pageSize) {
        return new PageResult<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), list, total, pageNum, pageSize);
    }

    public static <T> PageResult<T> success(String message, List<T> list, long total, int pageNum, int pageSize) {
        return new PageResult<>(ResultCode.SUCCESS.getCode(), message, list, total, pageNum, pageSize);
    }

    public static <T> PageResult<T> fail() {
        return new PageResult<>(ResultCode.FAIL.getCode(), ResultCode.FAIL.getMessage(), null, 0L, 0, 0);
    }

    public static <T> PageResult<T> fail(String message) {
        return new PageResult<>(ResultCode.FAIL.getCode(), message, null, 0L, 0, 0);
    }

    public static <T> PageResult<T> empty(int pageNum, int pageSize) {
        return new PageResult<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), List.of(), 0L, pageNum, pageSize);
    }

    public static <E, T> PageResult<T> convert(com.baomidou.mybatisplus.core.metadata.IPage<E> page, Function<E, T> converter) {
        List<T> list = page.getRecords().stream()
                .map(converter)
                .collect(Collectors.toList());
        return new PageResult<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), 
                list, page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }
}