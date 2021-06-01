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
        SomeService service = (SomeService) ac.getBean("someService");
        String hello = service.hello("China");
        System.out.println(hello);
    }
}
