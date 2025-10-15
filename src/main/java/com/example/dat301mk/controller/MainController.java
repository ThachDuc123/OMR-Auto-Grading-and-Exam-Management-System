package com.example.dat301mk.controller;

import com.example.dat301mk.entity.PasswordResetToken;
import com.example.dat301mk.entity.Users;
import com.example.dat301mk.repository.PasswordResetTokenRepository;
import com.example.dat301mk.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.security.Principal;
import com.example.dat301mk.entity.dto.ClassInfoDTO;
import com.example.dat301mk.service.StudentService;
import com.example.dat301mk.entity.dto.ResultInfoDTO;


@Controller
@RequestMapping("/") // Đặt tất cả các ánh xạ dưới gốc "/"
public class MainController {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private org.springframework.mail.javamail.JavaMailSender mailSender;

    @GetMapping("/")
    public String index() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/student_home")
    public String student_home(Model model, Principal principal) {
        String username = principal != null ? principal.getName() : null;
        Users user = null;
        if (username != null) {
            user = userService.findByUsername(username);
            List<ClassInfoDTO> classInfos = studentService.getClassInfoForStudent(username);
            model.addAttribute("classInfos", classInfos);
            List<ResultInfoDTO> recentResults = studentService.getRecentResultsForStudent(username, 5); // Lấy 5 kết quả gần nhất
            model.addAttribute("recentResults", recentResults);
        }
        model.addAttribute("user", user);
        model.addAttribute("currentPath", "/student_home");
        return "student_home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
                           @RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String role,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        boolean hasError = false;

        if (userService.findByUsername(username) != null) {
            model.addAttribute("usernameError", "Username already exists");
            hasError = true;
        }
        if (userService.findByEmail(email) != null) {
            model.addAttribute("emailError", "Email already exists");
            hasError = true;
        }

        if (hasError) {
            model.addAttribute("fullName", fullName);
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "register";
        }
        Users newUser = new Users();
        newUser.setFullName(fullName);
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setAvatarUrl("/img/user.png");
        newUser.setDeleted(false);
        newUser.setRole(role); // Sử dụng vai trò từ form
        newUser.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        userService.saveUser(newUser);

        redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
        return "redirect:/login";
    }

    @GetMapping("/forgotPassword")
    public String forgotPassword() {
        return "forgotPassword";
    }

    @PostMapping("/forgotPassword")
    public String forgotPassword(HttpServletRequest request,
                                 @RequestParam String email,
                                 RedirectAttributes redirectAttributes) {

        Users user = userService.findByEmail(email);
        if (user == null) {
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("emailError", "Email cannot be found. Please try again.");
            return "redirect:/forgotPassword";
        }

        String token = UUID.randomUUID().toString();
        PasswordResetToken myToken = new PasswordResetToken();
        myToken.setToken(token);
        myToken.setUser(user);
        passwordResetTokenRepository.save(myToken);

        String appUrl = request.getScheme() + "://" + request.getServerName() + ":" +
                request.getServerPort() + request.getContextPath();
        String url = appUrl + "/resetPassword?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Reset Password");
        message.setText("Click here to reset password:" + "\r\n" +
                url + "\r\n" +
                "The link will expire in 1 hour after the time sent of this email.");
        message.setFrom("no-reply.token-email@gmail.com");

        mailSender.send(message);

        redirectAttributes.addFlashAttribute("message", "A password reset link has been sent to " + user.getEmail() + ".");
        return "redirect:/forgotPassword";
    }

    @GetMapping("/overview")
    public String overview(Model model, Principal principal) {
        String username = principal != null ? principal.getName() : null;
        Users user = null;
        if (username != null) {
            user = userService.findByUsername(username);
        }
        model.addAttribute("user", user);
        model.addAttribute("currentPath", "/overview");
        return "overview";
    }

    @GetMapping("/class")
    public String classPage(Model model, Principal principal) {
        String username = principal != null ? principal.getName() : null;
        Users user = null;
        if (username != null) {
            user = userService.findByUsername(username);
        }
        model.addAttribute("user", user);
        model.addAttribute("currentPath", "/class");
        return "class";
    }

}