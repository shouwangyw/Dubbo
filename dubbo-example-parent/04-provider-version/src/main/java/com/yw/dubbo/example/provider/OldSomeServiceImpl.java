package com.yw.dubbo.example.provider;

import com.yw.dubbo.example.service.SomeService;

/**
 * 业务接口实现类
 * @author yangwei
 */
public class OldSomeServiceImpl implements SomeService {
    @Override
    public String hello(String name) {
        System.out.println("执行【老】的提供者OldSomeServiceImpl的hello()");
        return "OldSomeServiceImpl";
    }
}
