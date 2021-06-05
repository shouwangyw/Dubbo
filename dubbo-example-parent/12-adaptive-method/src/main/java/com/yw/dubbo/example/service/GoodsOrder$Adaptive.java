package com.yw.dubbo.example.service;

import org.apache.dubbo.common.extension.ExtensionLoader;

public class GoodsOrder$Adaptive implements com.yw.dubbo.example.service.GoodsOrder {
    public java.lang.String pay(org.apache.dubbo.common.URL arg0) {
        if (arg0 == null) throw new IllegalArgumentException("url == null");
        org.apache.dubbo.common.URL url = arg0;
        String extName = url.getParameter("goods.order", "alipay");
        if (extName == null)
            throw new IllegalStateException("Fail to get extension(com.yw.dubbo.example.service.GoodsOrder) name from url(" + url.toString() + ") use keys([goods.order])");
        com.yw.dubbo.example.service.GoodsOrder extension = (com.yw.dubbo.example.service.GoodsOrder) ExtensionLoader.getExtensionLoader(com.yw.dubbo.example.service.GoodsOrder.class).getExtension(extName);
        return extension.pay(arg0);
    }

    public java.lang.String way() {
        throw new UnsupportedOperationException("method public abstract java.lang.String com.yw.dubbo.example.service.GoodsOrder.way() of interface com.yw.dubbo.example.service.GoodsOrder is not adaptive method!");
    }
}