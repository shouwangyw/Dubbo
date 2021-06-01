package com.yw.dubbo.example.provider;

import com.yw.dubbo.example.service.SomeService;

/**
 * 业务接口实现类
 * @author yangwei
 */
public class NewSomeServiceImpl implements SomeService {
    @Override
    public String hello(String name) {
        System.out.println("执行【新】的提供者NewSomeServiceImpl的hello()");
        return "NewSomeServiceImpl";
    }
}
