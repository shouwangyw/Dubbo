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

//        System.out.println(service.hello("Tom"));
//        System.out.println(service.hello("Jerry"));
//        System.out.println(service.hello("Tom"));
//        System.out.println(service.hello("Jerry"));

        // 1000次不同的消费结果，将占满1000个缓存空间
        for (int i = 0; i < 1000; i++) {
            service.hello("i==" + i);
        }
        // 第1001次不同的消费结果，会将第一个缓存内容挤出去
        System.out.println(service.hello("Tom"));
        // 本次消费会重新调用提供者，说明原理的第一个缓存的确被挤出去了
        // 本次消费结果会将(i==2)的缓存结果挤出去
        System.out.println(service.hello("i==1"));
        // 本次消费会直接从缓存中获取
        System.out.println(service.hello("i==3"));
    }
}
