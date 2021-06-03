package com.yw.dubbo.example;

import com.yw.dubbo.example.service.SomeService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 消费者启动类
 *
 * @author yangwei
 */
public class ConsumerRun {
    public static void main(String[] args) {
        ApplicationContext ac = new ClassPathXmlApplicationContext("spring-consumer.xml");

        // 使用微信支付
        SomeService weixinService = (SomeService) ac.getBean("weixin");
        String weixin = weixinService.hello("China");
        System.out.println(weixin);

        // 使用支付宝支付
        SomeService zhifubaoService = (SomeService) ac.getBean("zhifubao");
        String zhifubao = zhifubaoService.hello("China");
        System.out.println(zhifubao);
    }
}
