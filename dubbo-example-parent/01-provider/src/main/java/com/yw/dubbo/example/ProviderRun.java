package com.yw.dubbo.example;

import org.apache.dubbo.container.Main;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 提供者启动类
 * @author yangwei
 */
public class ProviderRun {
    // 启动方式一：
//    public static void main(String[] args) throws Exception {
//        // 创建Spring容器
//        ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("spring-provider.xml");
//        // 启动Spring容器
//        ac.start();
//        // 使主线程阻塞
//        System.in.read();
//    }
    // 启动方式二：要求Spring配置文件必须要放到类路径下的 META-INF/spring 目录中
    public static void main(String[] args) {
        Main.main(args);
    }
}
