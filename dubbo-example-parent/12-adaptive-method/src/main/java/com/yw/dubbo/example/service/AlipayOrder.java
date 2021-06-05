package com.yw.dubbo.example.service;

import org.apache.dubbo.common.URL;

/**
 * @author yangwei
 */
//public class AlipayOrder implements Order {
public class AlipayOrder implements GoodsOrder {
    @Override
    public String way() {
        System.out.println("--- 使用支付宝支付 ---");
        return "支付宝支付";
    }

    @Override
    public String pay(URL url) {
        System.out.println("--- pay 使用支付宝支付 ---");
        return "pay 支付宝支付";
    }
}
