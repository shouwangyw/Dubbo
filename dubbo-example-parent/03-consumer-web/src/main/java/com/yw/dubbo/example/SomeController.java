package com.yw.dubbo.example;

import com.yw.dubbo.example.service.SomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 消费者控制器
 * @author yangwei
 */
@Controller
public class SomeController {
    @Autowired
    private SomeService someService;

    @RequestMapping("/some.do")
    public String someHandle() {
        System.err.println("消费者端接收到：" + someService.hello("China"));
        return "welcome.jsp";
    }
}
