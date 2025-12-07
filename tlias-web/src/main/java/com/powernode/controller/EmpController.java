package com.powernode.controller;

import com.powernode.pojo.Emp;
import com.powernode.pojo.Result;
import com.powernode.pojo.PageResult;
import com.powernode.service.EmpService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * @author CaoRui
 * @version 1.0
 * @createTime 2025-12-06
 * @description
 * @since 1.0
 */
@RequestMapping("/emps")
@RestController
public class EmpController {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(DeptController.class);
    @Autowired
    private EmpService empService;

    @GetMapping
    public Result page(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       String name,Integer gender,
                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start,
                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("分页查询员工信息，当前页：{}，每页记录数：{}",page,pageSize);
        PageResult<Emp> pageResult = empService.Page(page,pageSize,name,gender,start,end);
        return Result.success(pageResult);
    }
}
