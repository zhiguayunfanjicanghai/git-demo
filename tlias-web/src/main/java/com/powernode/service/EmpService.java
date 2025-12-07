package com.powernode.service;

import com.powernode.pojo.Emp;
import com.powernode.pojo.PageResult;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/**
 * @author CaoRui
 * @version 1.0
 * @createTime 2025-12-06
 * @description
 * @since 1.0
 */
public interface EmpService {
    PageResult<Emp> Page(Integer page, Integer pageSize, String name, Integer gender, LocalDate start, LocalDate end);
}

