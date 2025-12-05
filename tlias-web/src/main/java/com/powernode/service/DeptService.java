package com.powernode.service;

import com.powernode.pojo.Dept;

import java.util.List;

/**
 * @author CaoRui
 * @version 1.0
 * @createTime 2025-12-01
 * @description
 * @since 1.0
 */
public interface DeptService {
    List<Dept> findAll();

    void deleteById(Integer id);
}

