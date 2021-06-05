package com.yw.dubbo.example;

import com.yw.dubbo.example.service.AdaptiveOrder;
import com.yw.dubbo.example.service.Order;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.junit.Test;

/**
 * @author yangwei
 */
public class AdaptiveTest {
    @Test
    public void test01() {
        ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
        Order order = loader.getAdaptiveExtension();
        System.out.println(order.way());
    }

    @Test
    public void test02() {
        ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
        Order order = loader.getAdaptiveExtension();
        ((AdaptiveOrder) order).setDefaultName("wechat");
        System.out.println(order.way());
    }

}
