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

    /**
     * 下面代码用于演示负载均衡
     */
//    @Override
//    public String hello(String name) {
//        System.out.println("执行【第一个】提供者的hello() " + name);
//        return "【第一个】提供者";
//    }

//    @Override
//    public String hello(String name) {
//        System.out.println("执行【第二个】提供者的hello() " + name);
//        return "【第二个】提供者";
//    }

//    @Override
//    public String hello(String name) {
//        System.out.println("执行【第三个】提供者的hello() " + name);
//        return "【第三个】提供者";
//    }
}
