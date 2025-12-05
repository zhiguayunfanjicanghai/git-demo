package com.powernode.controller;

import com.powernode.pojo.Dept;
import com.powernode.pojo.Result;
import com.powernode.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author CaoRui
 * @version 1.0
 * @createTime 2025-12-01
 * @description
 * @since 1.0
 */
@RestController
public class DeptController {

    @Autowired
    private DeptService deptService;

    /**
     * 查询全部部门数据
     * @return
     */
    @GetMapping("/depts")
    public Result List(){
        System.out.println("查询全部部门数据");
        List<Dept> depts = deptService.findAll();
        return Result.success(depts);
    }

    /**
     * 删除部门
     * @return
     */
    @DeleteMapping("/depts")
    public Result delete(Integer  id){
        System.out.println("删除的部门部门是" + id);
        deptService.deleteById(id);
        return Result.success();
    }

}
