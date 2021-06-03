package com.yw.dubbo.example;

import com.yw.dubbo.example.service.OtherService;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * 消费者启动类
 *
 * @author yangwei
 */
public class ConsumerRun {
    public static void main(String[] args) throws Exception {
        ApplicationContext ac = new ClassPathXmlApplicationContext("spring-consumer.xml");
        OtherService service = (OtherService) ac.getBean("otherService");

        // 测试一：同步消费者
        // 记录同步调用开始时间
        long syncStart = System.currentTimeMillis();
        // 同步调用：
        System.out.println("同步，doFirst()直接获取到返回值：" + service.doFirst());
        System.out.println("同步，doSecond()直接获取到返回值：" + service.doSecond());
        System.out.println("两个同步操作共计用时(毫秒)：" + (System.currentTimeMillis() - syncStart));

        // 测试二：异步消费者，无返回值
        // 记录异步调用开始时间
        long asyncStart = System.currentTimeMillis();
        // 异步调用：
        service.doThird();
        service.doFourth();
        System.out.println("两个异步操作共计用时(毫秒)：" + (System.currentTimeMillis() - asyncStart));

        // 测试三：异步消费者，有返回值
        // 记录异步调用开始时间
        long asyncStart2 = System.currentTimeMillis();
        // 异步调用
        System.out.println("调用结果1 = " + service.doThird());
        Future<String> future = RpcContext.getContext().getFuture();
        System.out.println("调用结果2 = " + future.get());
        System.out.println("获取到异步调用结果共计用时(毫秒)：" + (System.currentTimeMillis() - asyncStart2));

        // 测试四：异步消费者，有返回值，使用CompletableFuture
        // 记录异步调用开始时间
        long asyncStart3 = System.currentTimeMillis();
        // 异步调用
        CompletableFuture<String> doThirdFuture = service.doThird();
        CompletableFuture<String> doFourthFuture = service.doFourth();
        System.out.println("两个异步操作共计用时(毫秒)：" + (System.currentTimeMillis() - asyncStart3));
        doThirdFuture.whenComplete((result, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            } else {
                System.out.println("异步调用提供者的doThird()返回值：" + result);
            }
        });
        doFourthFuture.whenComplete((result, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            } else {
                System.out.println("异步调用提供者的doFourth()返回值：" + result);
            }
        });
        System.out.println("获取到异步调用结果共计用时(毫秒)：" + (System.currentTimeMillis() - asyncStart3));
    }
}
