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
                    .filter(result -> {
                        Test resTest = result.getTest();
                        if (resTest == null) return false;
                        Classes testClasses = resTest.getClasses();
                        if (classes == null || testClasses == null) return false;
                        Long classId = classes.getId();
                        Long testClassId = testClasses.getId();
                        return classId != null && classId.equals(testClassId);
                    })
                    .collect(Collectors.toList());

            double averageScore = results.stream()
                    .mapToDouble(Result::getTotalScore)
                    .average()
                    .orElse(0.0);

            return new ClassInfoDTO(
                    classes != null ? classes.getId() : null,
                    classes != null ? classes.getClassCode() : null,
                    classes != null ? classes.getClassName() : null,
                    (classes != null && classes.getTeacher() != null) ? classes.getTeacher().getFullName() : null,
                    tests != null ? tests.size() : 0,
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
                .sorted((r1, r2) -> {
                    if (r1.getGradedAt() == null && r2.getGradedAt() == null) return 0;
                    if (r1.getGradedAt() == null) return 1; // nulls last
                    if (r2.getGradedAt() == null) return -1;
                    return r2.getGradedAt().compareTo(r1.getGradedAt());
                })
                .limit(limit)
                .map(result -> {
                    Test test = result.getTest();
                    String subjectName = null;
                    String title = null;
                    if (test != null) {
                        title = test.getTitle();
                        if (test.getSubject() != null) {
                            subjectName = test.getSubject().getSubjectName();
                        }
                    }
                    return new ResultInfoDTO(
                            subjectName,
                            title,
                            result.getGradedAt(),
                            result.getTotalScore(),
                            result.getMaxScore(),
                            result.getId()
                    );
                })
                .collect(Collectors.toList());
    }
}
