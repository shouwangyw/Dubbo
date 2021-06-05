package com.yw.dubbo.example.service;

/**
 * 业务接口
 * @author yangwei
 */
public class SomeServiceTwoImpl implements SomeService {
    @Override
    public String hello(String name) {
        System.out.println("执行SomeServiceTwoImpl的hello()方法");
        return "hello " + name;
    }
}
