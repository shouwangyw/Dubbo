package com.yw.dubbo.example.service;

import org.apache.dubbo.common.extension.Activate;

/**
 * @author yangwei
 */
//@Activate(group = "online")
// 若 group 属性和 value 属性同时存在，则是“与”的结果
@Activate(group = "online", value = "alipay")
public class AlipayOrder implements Order {
    @Override
    public String way() {
        System.out.println("--- 使用支付宝支付 ---");
        return "支付宝支付";
    }
}
