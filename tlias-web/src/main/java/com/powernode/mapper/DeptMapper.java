package com.powernode.mapper;

import com.powernode.pojo.Dept;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author CaoRui
 * @version 1.0
 * @createTime 2025-12-01
 * @description
 * @since 1.0
 */
@Mapper
public interface DeptMapper {


    /**
     * 查询全部部门数据
     * @return
     */
    @Select("select id,name,create_time,update_time from dept order by update_time desc")
    List<Dept> list();

    /**
     * 根据id删除部门
     * @param id
     */
    @Delete("delete from dept where id=#{id}")
    void deleteById(Integer id);
}

