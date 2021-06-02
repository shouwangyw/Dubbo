package com.yw.dubbo.example.service;

public interface UserService {
    String getUsernameById(int id);
    void addUser(String username);
}