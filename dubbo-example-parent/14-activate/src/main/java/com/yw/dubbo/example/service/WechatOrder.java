package com.yw.dubbo.example.service;

import org.apache.dubbo.common.extension.Activate;

/**
 * @author yangwei
 */
@Activate(group = "online")
public class WechatOrder implements Order {
    @Override
    public String way() {
        System.out.println("--- 使用微信支付 ---");
        return "微信支付";
    }
}
