package com.yw.dubbo.example.provider;

import com.yw.dubbo.example.service.UserService;

import java.util.concurrent.TimeUnit;

public class UserServiceImpl implements UserService {
    @Override
    public String getUsernameById(int id) {
        try{
            TimeUnit.SECONDS.sleep(2);
        }catch(Exception e){
        }
        // 这里暂且返回一个指定的值
        return "张三";
    }

    @Override
    public void addUser(String username) {
        // 这里暂且返回一个指定的成功提示
        System.out.println("添加用户成功");
    }
}