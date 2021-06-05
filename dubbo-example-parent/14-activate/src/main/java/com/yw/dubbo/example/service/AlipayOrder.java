package com.yw.dubbo.example.service;

import org.apache.dubbo.common.extension.Activate;

/**
 * @author yangwei
 */
@Activate(group = "online")
public class AlipayOrder implements Order {
    @Override
    public String way() {
        System.out.println("--- 使用支付宝支付 ---");
        return "支付宝支付";
    }
}
