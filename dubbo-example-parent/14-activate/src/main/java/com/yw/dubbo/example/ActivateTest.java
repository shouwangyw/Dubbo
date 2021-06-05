package com.yw.dubbo.example;

import com.yw.dubbo.example.service.Order;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.junit.Test;

import java.util.List;
import java.util.Set;

/**
 * @author yangwei
 */
public class ActivateTest {
    @Test
    public void test01() {
        ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
        URL url = URL.valueOf("xxx://localhost:8080/ooo/xxx");
        // 激活所有线上支付方式（group为online的扩展类）
        List<Order> orders = loader.getActivateExtension(url, "", "online");
        for (Order order : orders) {
            System.out.println(order.way());
        }
    }

    @Test
    public void test02() {
        ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
        URL url = URL.valueOf("xxx://localhost:8080/ooo/xxx");
        // 激活所有线下支付方式（group为offline的扩展类）
        List<Order> orders = loader.getActivateExtension(url, "", "offline");
        for (Order order : orders) {
            System.out.println(order.way());
        }
    }

    @Test
    public void test03() {
        ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
        URL url = URL.valueOf("xxx://localhost:8080/ooo/xxx?order=alipay");
        // 激活所有线上支付方式（group为online的扩展类）
        List<Order> orders = loader.getActivateExtension(url, "order", "online");
        for (Order order : orders) {
            System.out.println(order.way());
        }
    }

    @Test
    public void test04() {
        ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
        URL url = URL.valueOf("xxx://localhost:8080/ooo/xxx?order=alipay");
        // 激活所有线下支付方式（group为offline的扩展类）
        // getActivateExtension 的后两个参数是选择激活类的两个条件，是 或 的关系
        List<Order> orders = loader.getActivateExtension(url, "order", "offline");
        for (Order order : orders) {
            System.out.println(order.way());
        }
    }

    /**
     * Activate类是直接扩展类
     */
    @Test
    public void test05() {
        ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
        // 获取该SPI接口的所有直接扩展类：即该扩展类直接对该SPI接口进行业务功能上的扩展，可以单独使用
        Set<String> extensions = loader.getSupportedExtensions();
        System.out.println(extensions);
    }
}
