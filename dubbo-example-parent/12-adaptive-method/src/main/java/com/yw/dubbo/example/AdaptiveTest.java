package com.yw.dubbo.example;

import com.yw.dubbo.example.service.GoodsOrder;
import com.yw.dubbo.example.service.Order;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.junit.Test;

/**
 * @author yangwei
 */
public class AdaptiveTest {
    @Test
    public void test01() {
        // 获取SPI接口Order的loader实例
        ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
        // 获取到Order的自适应实例
        Order order = loader.getAdaptiveExtension();
        URL url = URL.valueOf("xxx://localhost:8080/ooo/xxx");
        System.out.println(order.pay(url));
        System.out.println(order.way());
    }

    @Test
    public void test02() {
        // 获取SPI接口Order的loader实例
        ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
        // 获取到Order的自适应实例
        Order order = loader.getAdaptiveExtension();
        URL url = URL.valueOf("xxx://localhost:8080/ooo/xxx?order=wechat");
        System.out.println(order.pay(url));
        System.out.println(order.way());
    }

    @Test
    public void test03() {
        // 获取SPI接口GoodsOrder的loader实例
        ExtensionLoader<GoodsOrder> loader = ExtensionLoader.getExtensionLoader(GoodsOrder.class);
        // 获取到GoodsOrder的自适应实例
        GoodsOrder order = loader.getAdaptiveExtension();
        URL url = URL.valueOf("xxx://localhost:8080/ooo/xxx?goods.order=wechat");
        System.out.println(order.pay(url));
        System.out.println(order.way());
    }

}
