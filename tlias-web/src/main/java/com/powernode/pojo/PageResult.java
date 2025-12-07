package com.powernode.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author CaoRui
 * @version 1.0
 * @createTime 2025-12-07
 * @description
 * @since 1.0
 */
@Data
public class PageResult<T> {
    private Long total;          // 总记录数
    private List<T> rows;        // 当前页数据


    public PageResult() {
    }

    public PageResult(Long total, List<T> rows) {
        this.total = total;
        this.rows = rows;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }
}