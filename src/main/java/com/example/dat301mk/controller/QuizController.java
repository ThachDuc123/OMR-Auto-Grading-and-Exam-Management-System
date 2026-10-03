package com.example.dat301mk.controller;

import com.example.dat301mk.entity.Question;
import com.example.dat301mk.service.QuizService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/quiz")
public class QuizController {
    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    public String showQuiz(Model model) {
        List<Question> questions = quizService.getAllQuestions();
        model.addAttribute("questions", questions);
        return "quiz";
    }

    @PostMapping("/submit")
    public String submitQuiz(@RequestParam("username") String username,
                             @RequestParam("answers") List<String> answers,
                             Model model) {
        int score = quizService.calculateScore(answers);
        quizService.saveSubmission(username, score);
        model.addAttribute("score", score);
        return "result";
    }
}

