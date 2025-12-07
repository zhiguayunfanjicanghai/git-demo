package com.powernode.controller;

import com.powernode.pojo.Dept;
import com.powernode.pojo.Result;
import com.powernode.service.DeptService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

/**
 * @author CaoRui
 * @version 1.0
 * @createTime 2025-12-01
 * @description
 * @since 1.0
 */
@RequestMapping("/depts")
@RestController
public class DeptController {

   private static final org.slf4j.Logger log = LoggerFactory.getLogger(DeptController.class);


    @Autowired
    private DeptService deptService;

    /**
     * 查询全部部门数据
     * @return
     */
    @GetMapping
    public Result List(){
        log.info("查询全部部门数据");
        List<Dept> depts = deptService.findAll();
        return Result.success(depts);
    }

    /**
     * 删除部门
     * @return
     */
    @DeleteMapping
    public Result delete(Integer  id){
        log.info("删除的部门部门是 {}" , id);
        deptService.deleteById(id);
        return Result.success();
    }

    /**
     * 添加部门
     * @return
     */
    @PostMapping
    public Result add(@RequestBody Dept dept){
       log.info("添加部门数据 {}" ,dept.getName());
        deptService.add(dept);
        return Result.success();
    }

    /**
     * 根据id获取部门信息
     * @return
     */
    @GetMapping("/{id}")
    public Result getInfo(@PathVariable Integer id){
        log.info("根据id获取部门信息 {}",id);
        Dept dept = deptService.getById(id);
        return Result.success(dept);
    }

    /**
     * 修改部门信息
     * @return
     */

    @PutMapping
    public Result update(@RequestBody Dept dept){
        log.info("修改部门数据 {}" , dept.getName());
        deptService.update(dept);
        return Result.success();
    }

}
