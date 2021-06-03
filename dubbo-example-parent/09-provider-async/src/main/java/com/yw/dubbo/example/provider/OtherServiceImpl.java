package com.yw.dubbo.example.provider;

import com.yw.dubbo.example.service.OtherService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author yangwei
 * @date 2021-06-04 00:29
 */
public class OtherServiceImpl implements OtherService {
    @Override
    public String doFirst() {
        sleep("doFirst");
        return "doFirst()";
    }

    @Override
    public String doSecond() {
        sleep("doSecond");
        return "doSecond()";
    }

//    @Override
//    public String doThird() {
//        sleep("doThird");
//        return "doThird()";
//    }
//    @Override
//    public String doFourth() {
//        sleep("doFourth");
//        return "doFourth()";
//    }

//    @Override
//    public CompletableFuture<String> doThird() {
//        sleep("doThird");
//        return CompletableFuture.completedFuture("doThird()");
//    }
//    @Override
//    public CompletableFuture<String> doFourth() {
//        sleep("doFourth");
//        return CompletableFuture.completedFuture("doFourth()");
//    }

    /**
     * 提供者进行异步执行
     */
    @Override
    public CompletableFuture<String> doThird() {
        long startTime = System.currentTimeMillis();
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            sleep("doThird");
            return "doThird";
        });
        System.out.println("doThird()方法执行耗时(毫秒)：" + (System.currentTimeMillis() - startTime));
        return future;
    }
    @Override
    public CompletableFuture<String> doFourth() {
        long startTime = System.currentTimeMillis();
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            sleep("doFourth");
            return "doFourth";
        });
        System.out.println("doFourth()方法执行耗时(毫秒)：" + (System.currentTimeMillis() - startTime));
        return future;
    }

    // 模拟耗时操作
    private void sleep(String method) {
        long startTime = System.currentTimeMillis();
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (Exception ignored) {
        }
        System.out.println(method + "方法执行用时：" + (System.currentTimeMillis() - startTime));
    }
}
