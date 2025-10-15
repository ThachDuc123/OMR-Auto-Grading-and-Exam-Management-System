package com.example.dat301mk.service;

import com.example.dat301mk.entity.Question;
import com.example.dat301mk.entity.Submission;
import com.example.dat301mk.repository.QuestionRepository;
import com.example.dat301mk.repository.SubmissionRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class QuizService {
    private final QuestionRepository questionRepo;
    private final SubmissionRepository submissionRepo;

    public QuizService(QuestionRepository questionRepo, SubmissionRepository submissionRepo) {
        this.questionRepo = questionRepo;
        this.submissionRepo = submissionRepo;
    }

    public List<Question> getAllQuestions() {
        return questionRepo.findAll();
    }

    public int calculateScore(List<String> answers) {
        List<Question> questions = questionRepo.findAll();
        int score = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (i < answers.size() && questions.get(i).getCorrectAnswer().equalsIgnoreCase(answers.get(i))) {
                score++;
            }
        }
        return score;
    }

    public Submission saveSubmission(String username, int score) {
        Submission sub = new Submission();
        sub.setUsername(username);
        sub.setScore(score);
        return submissionRepo.save(sub);
    }
}
