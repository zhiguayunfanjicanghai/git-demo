package com.powernode.mapper;

import com.github.pagehelper.Page;
import com.powernode.pojo.Emp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * @author CaoRui
 * @version 1.0
 * @createTime 2025-12-06
 * @description
 * @since 1.0
 */
@Mapper
public interface EmpMapper {
    /**
     * 分页查询所有员工信息
     * @return
     */
    List<Emp> list(String name, Integer gender, LocalDate start, LocalDate end);
}

