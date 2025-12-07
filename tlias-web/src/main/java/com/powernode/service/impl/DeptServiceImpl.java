package com.powernode.service.impl;

import com.powernode.mapper.DeptMapper;
import com.powernode.pojo.Dept;
import com.powernode.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author CaoRui
 * @version 1.0
 * @createTime 2025-12-01
 * @description
 * @since 1.0
 */
@Service
public class DeptServiceImpl implements DeptService {

    @Autowired
    private DeptMapper deptMapper;

    /**
     * 查询全部部门数据
     * @return
     */
    @Override
    public List<Dept> findAll() {
        return deptMapper.list();
    }

    /**
     * 删除部门
     * @return
     */
    @Override
    public void deleteById(Integer id) {
        deptMapper.deleteById(id);
    }

    /**
     * 新增部门
     * @return
     */

    @Override
    public void add(Dept dept) {
        dept.setCreateTime(LocalDateTime.now());
        dept.setUpdateTime(LocalDateTime.now());
        deptMapper.insert(dept);
    }

    /**
     * 根据id获取部门信息
     * @return
     */
    @Override
    public Dept getById(Integer id) {
        return deptMapper.getById(id);
    }

    /**
     * 修改部门信息
     * @return
     */
    @Override
    public void update(Dept dept) {
        dept.setUpdateTime(LocalDateTime.now());
        deptMapper.update(dept);
    }
}
