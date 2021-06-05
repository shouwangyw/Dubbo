package com.yw.dubbo.example.service;

/**
 * 业务接口
 * @author yangwei
 */
public class SomeServiceOneImpl implements SomeService {
    @Override
    public String hello(String name) {
        System.out.println("执行SomeServiceOneImpl的hello()方法");
        return "hello " + name;
    }
}
