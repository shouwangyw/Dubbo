package com.yw.dubbo.example.service;

import org.apache.dubbo.common.extension.SPI;

/**
 * 下单接口
 *
 * @author yangwei
 */
@SPI("alipay")
public interface Order {
    String way();
}
