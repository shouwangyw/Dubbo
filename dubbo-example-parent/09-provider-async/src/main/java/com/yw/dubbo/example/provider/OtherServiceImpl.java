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

    @Override
    public CompletableFuture<String> doThird() {
        sleep("doThird");
        return CompletableFuture.completedFuture("doThird()");
    }
    @Override
    public CompletableFuture<String> doFourth() {
        sleep("doFourth");
        return CompletableFuture.completedFuture("doFourth()");
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
