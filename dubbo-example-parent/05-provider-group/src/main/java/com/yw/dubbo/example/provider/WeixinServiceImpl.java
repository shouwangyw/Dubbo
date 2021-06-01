package com.yw.dubbo.example.provider;

import com.yw.dubbo.example.service.SomeService;

public class WeixinServiceImpl implements SomeService {
    @Override
    public String hello(String name) {
        System.out.println("使用【微信】支付");
        return "WeixinServiceImpl";
    }
}