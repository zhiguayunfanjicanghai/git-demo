package com.powernode.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.powernode.mapper.EmpMapper;
import com.powernode.pojo.Emp;
import com.powernode.pojo.PageResult;
import com.powernode.service.EmpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * @author CaoRui
 * @version 1.0
 * @createTime 2025-12-06
 * @description
 * @since 1.0
 */
@Service
public class EmpServiceImpl implements EmpService {

    @Autowired
    private EmpMapper empMapper;

    /**
     * 分页查询员工信息
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult<Emp> Page(Integer page, Integer pageSize, String name, Integer gender, LocalDate start, LocalDate end) {
        PageHelper.startPage(page, pageSize);
        Page<Emp> pageResult = (Page<Emp>) empMapper.list(name, gender, start, end);
        return new PageResult<Emp>(pageResult.getTotal(), pageResult.getResult());
    }
}
