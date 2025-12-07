package com.powernode.mapper;

import com.powernode.pojo.Dept;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
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

    /**
     * 新增部门
     * @param dept
     */
    @Insert("insert into dept(name,create_time,update_time) values(#{name},#{createTime},#{updateTime})")
    void insert(Dept dept);

    /**
     * 根据id查询部门
     * @param id
     * @return
     */
    @Select("select id,name,create_time,update_time from dept where id=#{id}")
    Dept getById(Integer id);

    /**
     * 修改部门
     * @param dept
     */
    @Insert("update dept set name=#{name},update_time=#{updateTime} where id=#{id}")
    void update(Dept dept);
}

