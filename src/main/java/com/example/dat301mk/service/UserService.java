package com.example.dat301mk.service;

import com.example.dat301mk.entity.Users;

public interface UserService {
    Users findByUsername(String username);
    Users findByEmail(String email);
    void saveUser(Users user);
}