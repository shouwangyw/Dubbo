package com.yw.dubbo.example.service;

import org.apache.dubbo.common.extension.Adaptive;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.springframework.util.StringUtils;

/**
 * @author yangwei
 */
@Adaptive
public class AdaptiveOrder implements Order {
    private String defaultName;

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    @Override
    public String way() {
        ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
        Order order = StringUtils.isEmpty(defaultName) ? loader.getDefaultExtension()
                : loader.getExtension(defaultName);
        return order.way();
    }
}
