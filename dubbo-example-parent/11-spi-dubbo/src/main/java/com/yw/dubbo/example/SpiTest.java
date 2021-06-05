package com.yw.dubbo.example;

import com.yw.dubbo.example.service.Order;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.junit.Test;

/**
 * @author yangwei
 */
public class SpiTest {
    @Test
    public void test01() {
        // 获取SPI接口Order的loader实例
        ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
        // 指定要加载并创建的扩展类实例
        Order alipay = loader.getExtension("alipay");
        System.out.println(alipay.way());
        Order wechat = loader.getExtension("wechat");
        System.out.println(wechat.way());

        Order xxx = loader.getExtension("xxx");
        System.out.println(xxx.way());
    }

    @Test
    public void test02() {
        // 获取SPI接口Order的loader实例
        ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
        Order order = loader.getExtension(null);
        System.out.println(order.way());
    }

    @Test
    public void test03() {
        // 获取SPI接口Order的loader实例
        ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
        Order order = loader.getExtension("true");
        System.out.println(order.way());
    }
}
