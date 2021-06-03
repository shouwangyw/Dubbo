package com.yw.dubbo.example.provider;

import com.yw.dubbo.example.service.SomeService;

public class ZhifubaoServiceImpl implements SomeService {
    @Override
    public String hello(String name) {
        System.out.println("使用【支付宝】支付");
        return "ZhifubaoServiceImpl";
    }
}