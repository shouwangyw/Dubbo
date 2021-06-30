package com.yw.dubbo.example.service;

import org.apache.dubbo.common.extension.ExtensionLoader;

public class Order$Adaptive implements com.yw.dubbo.example.service.Order {
    public java.lang.String pay(org.apache.dubbo.common.URL arg0) {
        if (arg0 == null) throw new IllegalArgumentException("url == null");
        org.apache.dubbo.common.URL url = arg0;
        String extName = url.getParameter("order", "alipay");
        if (extName == null)
            throw new IllegalStateException("Failed to get extension (com.yw.dubbo.example.service.Order) name from url (" + url.toString() + ") use keys([order])");
        com.yw.dubbo.example.service.Order extension = (com.yw.dubbo.example.service.Order) ExtensionLoader.getExtensionLoader(com.yw.dubbo.example.service.Order.class)
                .getExtension(extName);
        return extension.pay(arg0);
    }

    public java.lang.String way() {
        throw new UnsupportedOperationException("The method public abstract java.lang.String com.yw.dubbo.example.service.Order.way() of interface com.yw.dubbo.example.service.Order is not adaptive method!");
    }
}