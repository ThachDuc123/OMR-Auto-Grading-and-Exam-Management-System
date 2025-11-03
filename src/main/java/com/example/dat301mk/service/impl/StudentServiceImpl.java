package com.example.dat301mk.service.impl;

import com.example.dat301mk.entity.ClassMember;
import com.example.dat301mk.entity.Classes;
import com.example.dat301mk.entity.Result;
import com.example.dat301mk.entity.Test;
import com.example.dat301mk.entity.Users;
import com.example.dat301mk.entity.dto.ClassInfoDTO;
import com.example.dat301mk.entity.dto.ResultInfoDTO;
import com.example.dat301mk.repository.ClassMemberRepository;
import com.example.dat301mk.repository.ResultRepository;
import com.example.dat301mk.repository.TestRepository;
import com.example.dat301mk.repository.UserRepository;
import com.example.dat301mk.service.StudentService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl implements StudentService {

    private final UserRepository userRepository;
    private final ClassMemberRepository classMemberRepository;
    private final TestRepository testRepository;
    private final ResultRepository resultRepository;

    public StudentServiceImpl(UserRepository userRepository,
                              ClassMemberRepository classMemberRepository,
                              TestRepository testRepository,
                              ResultRepository resultRepository) {
        this.userRepository = userRepository;
        this.classMemberRepository = classMemberRepository;
        this.testRepository = testRepository;
        this.resultRepository = resultRepository;
    }

    @Override
    public List<ClassInfoDTO> getClassInfoForStudent(String username) {
        Optional<Users> studentOpt = userRepository.findByUsername(username);
        if (studentOpt.isEmpty()) {
            return Collections.emptyList();
        }
        Users student = studentOpt.get();
        List<ClassMember> classMembers = classMemberRepository.findByStudent(student);

        return classMembers.stream().map(classMember -> {
            Classes classes = classMember.getClasses();
            List<Test> tests = testRepository.findByClasses(classes);
            List<Result> results = resultRepository.findByStudent(student)
                    .stream()
                    .filter(result -> result.getTest().getClasses().equals(classes))
                    .collect(Collectors.toList());

            double averageScore = results.stream()
                    .mapToDouble(Result::getTotalScore)
                    .average()
                    .orElse(0.0);

            return new ClassInfoDTO(
                    classes.getId(),
                    classes.getClassCode(),
                    classes.getClassName(),
                    classes.getTeacher().getFullName(),
                    tests.size(),
                    averageScore
            );
        }).collect(Collectors.toList());
    }

    @Override
    public List<ResultInfoDTO> getRecentResultsForStudent(String username, int limit) {
        Optional<Users> studentOpt = userRepository.findByUsername(username);
        if (studentOpt.isEmpty()) {
            return Collections.emptyList();
        }
        Users student = studentOpt.get();
        List<Result> results = resultRepository.findByStudent(student);

        return results.stream()
                .sorted((r1, r2) -> r2.getGradedAt().compareTo(r1.getGradedAt()))
                .limit(limit)
                .map(result -> new ResultInfoDTO(
                        result.getTest().getSubject().getSubjectName(), // Fix: get subject name as String
                        result.getTest().getTitle(),
                        result.getGradedAt(),
                        result.getTotalScore(),
                        result.getMaxScore(),
                        result.getId()
                ))
                .collect(Collectors.toList());
    }
}
