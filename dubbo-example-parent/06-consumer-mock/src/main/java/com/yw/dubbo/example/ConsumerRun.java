package com.yw.dubbo.example;

import com.yw.dubbo.example.service.UserService;
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
        UserService service = (UserService) ac.getBean("userService");

        // 有返回值的方法降级结果为null
        String username = service.getUsernameById(3);
        System.out.println("username = " +username);

        // 无返回值的方法降级结果无任何显示
        service.addUser("China");
    }
}
