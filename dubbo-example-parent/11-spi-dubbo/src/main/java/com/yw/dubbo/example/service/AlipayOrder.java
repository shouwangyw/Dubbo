package com.yw.dubbo.example.service;

/**
 * @author yangwei
 * @date 2021-06-05 13:36
 */
public class AlipayOrder implements Order {
    @Override
    public String way() {
        System.out.println("--- 使用支付宝支付 ---");
        return "支付宝支付";
    }
}
