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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.security.Principal;
import com.example.dat301mk.entity.dto.ClassInfoDTO;
import com.example.dat301mk.service.StudentService;
import com.example.dat301mk.entity.dto.ResultInfoDTO;
import com.example.dat301mk.repository.ClassesRepository;
import com.example.dat301mk.entity.Classes;
import com.example.dat301mk.repository.ClassMemberRepository;
import com.example.dat301mk.entity.ClassMember;
import com.example.dat301mk.entity.dto.ClassStudentStatDTO;
import com.example.dat301mk.repository.OmrSheetRepository;
import com.example.dat301mk.repository.TestVersionRepository;
import com.example.dat301mk.repository.TestRepository;
import com.example.dat301mk.entity.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.web.multipart.MultipartFile;
import com.example.dat301mk.entity.TestVersion;
import com.example.dat301mk.entity.Subject;
import com.example.dat301mk.repository.SubjectRepository;


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

    @Autowired
    private ClassesRepository classesRepository;

    @Autowired
    private ClassMemberRepository classMemberRepository;

    @Autowired
    private OmrSheetRepository omrSheetRepository;
    @Autowired
    private TestVersionRepository testVersionRepository;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private SubjectRepository subjectRepository;

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

    @GetMapping("/student_class")
    public String classPage(Model model, Principal principal) {
        String username = principal != null ? principal.getName() : null;
        Users user = null;
        List<ClassStudentStatDTO> classList = List.of();
        List<Classes> pendingClassList = List.of();
        List<Classes> hiddenClassList = List.of();
        if (username != null) {
            user = userService.findByUsername(username);
            Users finalUser = user; // Make final for lambda
            List<ClassMember> classMembers = classMemberRepository.findByStudentAndStatusAndIsHidden(finalUser, "approved", false);
            classList = classMembers.stream().map(cm -> {
                Classes clazz = cm.getClasses();
                int soBaiDaThi = omrSheetRepository.countByTest_Classes_IdAndStudent_Id(clazz.getId(), Long.valueOf(finalUser.getId()));
                int soTaiLieu = testVersionRepository.countByTest_Classes_Id(clazz.getId());
                return new ClassStudentStatDTO(clazz.getId(), clazz.getClassName(), clazz.getClassCode(), soBaiDaThi, soTaiLieu);
            }).toList();
            List<ClassMember> pendingMembers = classMemberRepository.findByStudentAndStatus(finalUser, "pending");
            pendingClassList = pendingMembers.stream().map(ClassMember::getClasses).toList();
            List<ClassMember> hiddenMembers = classMemberRepository.findByStudentAndStatusAndIsHidden(finalUser, "approved", true);
            hiddenClassList = hiddenMembers.stream().map(ClassMember::getClasses).toList();
        }
        model.addAttribute("user", user);
        model.addAttribute("currentPath", "/student_class");
        model.addAttribute("classList", classList);
        model.addAttribute("pendingClassList", pendingClassList);
        model.addAttribute("hiddenClassList", hiddenClassList);
        return "student_class";
    }

    @GetMapping("/teacher_quiz")
    public String teacherQuizPage(Model model, Principal principal) {
        String username = principal != null ? principal.getName() : null;
        Users user = null;
        List<Test> testList = List.of();
        Map<String, Map<String, List<TestVersion>>> subjectTestMap = new LinkedHashMap<>();
        if (username != null) {
            user = userService.findByUsername(username);
            testList = testRepository.findAll();
            System.out.println("[DEBUG] testList size: " + testList.size());
            for (Test test : testList) {
                System.out.println("[DEBUG] Test: " + test.getTestCode() + ", Subject: " + (test.getSubject() != null ? test.getSubject().getSubjectName() : "null"));
                if (test.getPdfPath() != null) {
                    try {
                        java.nio.file.Path pdfPath = java.nio.file.Paths.get("src/main/resources/static" + test.getPdfPath());
                        if (java.nio.file.Files.exists(pdfPath)) {
                            long size = java.nio.file.Files.size(pdfPath);
                            test.setPdfSizeKb(size / 1024);
                        } else {
                            test.setPdfSizeKb(null);
                        }
                    } catch (Exception e) {
                        test.setPdfSizeKb(null);
                    }
                } else {
                    test.setPdfSizeKb(null);
                }
                String subjectName = (test.getSubject() != null && test.getSubject().getSubjectName() != null) ? test.getSubject().getSubjectName() : "Khác";
                String testCode = test.getTestCode();
                List<TestVersion> versions = testVersionRepository.findByTest(test);
                if (versions == null) versions = new ArrayList<>();
                System.out.println("[DEBUG] Test: " + testCode + ", versions: " + (versions != null ? versions.size() : 0));
                for (TestVersion v : versions) {
                    System.out.println("[DEBUG]   Version: " + v.getVersionCode() + ", filePath: " + v.getFilePath());
                }
                subjectTestMap.computeIfAbsent(subjectName, k -> new LinkedHashMap<>())
                        .computeIfAbsent(testCode, k -> new ArrayList<>())
                        .addAll(versions);
            }
        }
        System.out.println("[DEBUG] subjectTestMap size: " + subjectTestMap.size());
        for (String subj : subjectTestMap.keySet()) {
            System.out.println("[DEBUG] Subject: " + subj + ", tests: " + subjectTestMap.get(subj).size());
        }
        // Build a map from testCode -> Test for easy lookup in the template
        Map<String, Test> testByCode = new LinkedHashMap<>();
        for (Test t : testList) {
            if (t.getTestCode() != null) testByCode.put(t.getTestCode(), t);
        }

        model.addAttribute("user", user);
        model.addAttribute("subjectTestMap", subjectTestMap);
        model.addAttribute("testList", testList); // for backward compatibility
        model.addAttribute("testByCode", testByCode); // new helper map
        model.addAttribute("currentPath", "/teacher_quiz");
        return "teacher_quiz";
    }

    @GetMapping("/test/pdf/{testId}")
    public ResponseEntity<Resource> getTestPdf(@PathVariable Long testId) {
        Test test = testRepository.findById(testId).orElse(null);
        if (test == null || test.getPdfPath() == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            Path filePath = Paths.get("src/main/resources/static" + test.getPdfPath());
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) return ResponseEntity.notFound().build();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filePath.getFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/test/answer/{testId}")
    public ResponseEntity<Resource> getTestAnswer(@PathVariable Long testId) {
        Test test = testRepository.findById(testId).orElse(null);
        if (test == null || test.getCsvAnswerPath() == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            Path filePath = Paths.get("src/main/resources/static" + test.getCsvAnswerPath());
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) return ResponseEntity.notFound().build();
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "text/csv";
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filePath.getFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/teacher_quiz/upload")
    public String uploadTest(
                             @RequestParam String title,
                             @RequestParam String testCode,
                             @RequestParam String subject,
                             @RequestParam("pdfFile") MultipartFile pdfFile,
                             @RequestParam(value = "csvAnswer", required = false) MultipartFile csvAnswer,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        try {
            String username = principal != null ? principal.getName() : null;
            Users user = username != null ? userService.findByUsername(username) : null;
            // Lưu file PDF
            String pdfFileName = System.currentTimeMillis() + "_" + pdfFile.getOriginalFilename();
            String pdfPath = "/assets/pdf/" + pdfFileName;
            java.nio.file.Path pdfSavePath = java.nio.file.Paths.get("src/main/resources/static/assets/pdf/" + pdfFileName);
            java.nio.file.Files.createDirectories(pdfSavePath.getParent());
            pdfFile.transferTo(pdfSavePath);
            // Lưu file CSV đáp án nếu có
            String csvPath = null;
            if (csvAnswer != null && !csvAnswer.isEmpty()) {
                String csvFileName = System.currentTimeMillis() + "_" + csvAnswer.getOriginalFilename();
                csvPath = "/assets/answers/" + csvFileName;
                java.nio.file.Path csvSavePath = java.nio.file.Paths.get("src/main/resources/static/assets/answers/" + csvFileName);
                java.nio.file.Files.createDirectories(csvSavePath.getParent());
                csvAnswer.transferTo(csvSavePath);
            }
            // Find or create subject
            Subject subjectEntity = null;
            // You need a SubjectRepository bean for this, assumed as subjectRepository
            subjectEntity = subjectRepository.findBySubjectName(subject);
            if (subjectEntity == null) {
                subjectEntity = new Subject();
                subjectEntity.setSubjectName(subject);
                subjectEntity.setSubjectCode(subject.replaceAll("[^A-Za-z0-9]", "").toUpperCase());
                subjectRepository.save(subjectEntity);
            }
            Test test = new Test();
            test.setClasses(null); // Không cần chọn lớp
            test.setTitle(title);
            test.setTestCode(testCode);
            test.setSubject(subjectEntity);
            test.setPdfPath(pdfPath);
            test.setCsvAnswerPath(csvPath);
            test.setCreatedAt(Instant.now());
            test.setCreatedBy(user);
            testRepository.save(test);
            redirectAttributes.addFlashAttribute("success", "Tải đề thi lên thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tải đề thi: " + e.getMessage());
        }
        return "redirect:/teacher_quiz";
    }

    @GetMapping("/version/pdf/{versionId}")
    public ResponseEntity<Resource> getVersionPdf(@PathVariable Long versionId) {
        TestVersion version = testVersionRepository.findById(versionId).orElse(null);
        if (version == null || version.getFilePath() == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            Path filePath = Paths.get("src/main/resources/static" + version.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) return ResponseEntity.notFound().build();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filePath.getFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
