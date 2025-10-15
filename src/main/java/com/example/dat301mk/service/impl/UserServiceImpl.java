package com.example.dat301mk.service.impl;

import com.example.dat301mk.entity.Users;
import com.example.dat301mk.repository.UserRepository;
import com.example.dat301mk.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Users findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public Users findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public void saveUser(Users user) {
        userRepository.save(user);
    }
}

