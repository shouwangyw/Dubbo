package com.yw.dubbo.example.service;

import org.apache.dubbo.common.extension.Activate;

/**
 * @author yangwei
 */
// order属性的默认值为0，值越小，激活的优先级越高
@Activate(group = {"online", "offline"}, order = 3)
public class CardOrder implements Order {
    @Override
    public String way() {
        System.out.println("--- 使用银联卡支付 ---");
        return "银联卡支付";
    }
}
