package com.yw.dubbo.example.service;

import java.util.concurrent.CompletableFuture;

public interface OtherService {
    String doFirst();
    String doSecond();
//    String doThird();
//    String doFourth();

    /**
     * 异步调用返回结果
     */
    CompletableFuture<String> doThird();
    CompletableFuture<String> doFourth();
}