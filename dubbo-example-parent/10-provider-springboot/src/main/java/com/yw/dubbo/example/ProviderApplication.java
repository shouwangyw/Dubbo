package com.yw.dubbo.example;

import org.apache.dubbo.config.spring.context.annotation.DubboComponentScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 提供者启动类
 *
 * @author yangwei
 */
@EnableTransactionManagement    // 开启事务
@EnableCaching                  // 开启缓存
@DubboComponentScan(basePackages = "com.yw.dubbo.example.service") // 扫描提供者服务
@SpringBootApplication
public class ProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }
}
