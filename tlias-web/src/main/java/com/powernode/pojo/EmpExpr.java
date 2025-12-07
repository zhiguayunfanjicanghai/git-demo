package com.powernode.pojo;

import java.time.LocalDate;

/**
 * @author CaoRui
 * @version 1.0
 * @createTime 2025-12-06
 * @description
 * @since 1.0
 */
public class EmpExpr {
    private Integer id;         // ID, 主键
    private Integer empId;      // 员工ID
    private LocalDate begin;    // 开始时间
    private LocalDate end;      // 结束时间（在职可为 null）
    private String company;     // 公司名称
    private String job;         // 职位

    public EmpExpr() {
    }

    public EmpExpr(Integer id, Integer empId, LocalDate begin, LocalDate end, String company, String job) {
        this.id = id;
        this.empId = empId;
        this.begin = begin;
        this.end = end;
        this.company = company;
        this.job = job;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public LocalDate getBegin() {
        return begin;
    }

    public void setBegin(LocalDate begin) {
        this.begin = begin;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }
}
