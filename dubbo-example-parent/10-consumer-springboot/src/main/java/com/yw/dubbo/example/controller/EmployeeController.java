package com.yw.dubbo.example.controller;

import com.yw.dubbo.example.model.Employee;
import com.yw.dubbo.example.service.EmployeeService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

/**
 * @author yangwei
 */
@RestController
@RequestMapping("/consumer/employee")
public class EmployeeController {
//    @Autowired
    @Reference(version = "1.0.0") // Dubbo注解，<dubbo:reference />
    private EmployeeService employeeService;

    @PostMapping("/register")
    public String registerHandle(@RequestBody Employee employee){
        employeeService.addEmployee(employee);
        return "OK";
    }

    @GetMapping("/find/{id}")
    @ResponseBody
    public Employee findHandle(@PathVariable("id") int id){
        return employeeService.findEmployeeById(id);
    }

    @GetMapping("/count")
    @ResponseBody
    public Integer countHandle(){
        return employeeService.findEmployeeCount();
    }
}
