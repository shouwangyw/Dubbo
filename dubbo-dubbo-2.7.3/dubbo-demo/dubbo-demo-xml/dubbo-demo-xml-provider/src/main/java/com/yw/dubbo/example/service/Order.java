package com.yw.dubbo.example.service;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Adaptive;
import org.apache.dubbo.common.extension.SPI;

/**
 * 下单接口
 *
 * @author yangwei
 */
@SPI("alipay")
public interface Order {
    String way();

    @Adaptive
    String pay(URL url);
}
