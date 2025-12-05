package com.powernode.service.impl;

import com.powernode.mapper.DeptMapper;
import com.powernode.pojo.Dept;
import com.powernode.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public void deleteById(Integer id) {
        deptMapper.deleteById(id);
    }
}
