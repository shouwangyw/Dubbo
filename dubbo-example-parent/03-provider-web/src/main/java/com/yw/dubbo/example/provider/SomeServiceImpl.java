package com.yw.dubbo.example.provider;

import com.yw.dubbo.example.service.SomeService;

/**
 * 业务接口实现类
 * @author yangwei
 */
public class SomeServiceImpl implements SomeService {
    @Override
    public String hello(String name) {
        System.out.println(name + "，我是提供者");
        return "Hello Dubbo World! " + name;
    }
}
