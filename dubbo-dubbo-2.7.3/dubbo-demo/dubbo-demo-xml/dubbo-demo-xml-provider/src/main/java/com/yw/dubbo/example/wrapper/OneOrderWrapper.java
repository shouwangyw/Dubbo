package com.yw.dubbo.example.wrapper;

import com.yw.dubbo.example.service.Order;
import org.apache.dubbo.common.URL;

/**
 * @author yangwei
 */
public class OneOrderWrapper implements Order {
    private Order order;

    public OneOrderWrapper(Order order) {
        this.order = order;
    }

    @Override
    public String way() {
        System.out.println("Before OneOrderWrapper 对 way() 增强");
        String result = order.way();
        System.out.println("After OneOrderWrapper 对 way() 增强");
        return result;
    }

    @Override
    public String pay(URL url) {
        System.out.println("Before OneOrderWrapper 对 pay() 增强");
        String result = order.pay(url);
        System.out.println("After OneOrderWrapper 对 pay() 增强");
        return result;
    }
}
