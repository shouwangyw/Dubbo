package com.yw.dubbo.example.service;

import com.yw.dubbo.example.model.Employee;

public interface EmployeeService {
    void addEmployee(Employee employee);
    Employee findEmployeeById(int id);
    Integer findEmployeeCount();
}