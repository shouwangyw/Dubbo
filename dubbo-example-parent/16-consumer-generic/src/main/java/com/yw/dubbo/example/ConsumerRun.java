package com.yw.dubbo.example;

import org.apache.dubbo.rpc.service.GenericService;
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
        GenericService service = (GenericService) ac.getBean("someService");
        Object hello = service.$invoke("hello", new String[]{String.class.getName()}, new Object[]{"Tom"});
        System.out.println("generic =============== " + hello);
    }
}
