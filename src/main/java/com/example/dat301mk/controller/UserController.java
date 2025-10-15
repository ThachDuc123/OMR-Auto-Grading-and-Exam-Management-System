package com.example.dat301mk.controller;

import com.example.dat301mk.entity.Users;
import com.example.dat301mk.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UsersRepository usersRepository;

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register"; // Hiển thị form đăng ký
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // Hiển thị form đăng nhập
    }

    @GetMapping("/profile")
    public String showProfile(@RequestParam String username, Model model) {
        usersRepository.findByUsername(username).ifPresent(user -> model.addAttribute("user", user));
        return "profile"; // Hiển thị thông tin người dùng
    }

    @PostMapping("/register")
    public String register(@ModelAttribute Users user, Model model) {
        if (usersRepository.findByUsername(user.getUsername()) != null) {
            model.addAttribute("error", "Username already exists!");
            return "register";
        }
        usersRepository.save(user);
        model.addAttribute("message", "Registration successful!");
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model) {
        Users user = usersRepository.findByUsername(username).orElse(null);
        if (user != null && user.getPassword().equals(password)) {
            model.addAttribute("username", username);
            return "student_home";
        }
        model.addAttribute("error", "Invalid credentials");
        return "login";
    }
}
