package com.yw.dubbo.example.dao;

import com.yw.dubbo.example.model.Employee;
import org.apache.ibatis.annotations.Mapper;

// 自动Mapper的动态代理
@Mapper
public interface EmployeeDao {
    void insertEmployee(Employee employee);
    Integer selectEmployeeCount();
    Employee selectEmployeeById(int id);
}