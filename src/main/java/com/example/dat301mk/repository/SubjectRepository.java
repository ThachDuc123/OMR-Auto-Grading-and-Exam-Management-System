package com.example.dat301mk.repository;

import com.example.dat301mk.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Subject findBySubjectName(String subjectName);
    Subject findBySubjectCode(String subjectCode);
}

