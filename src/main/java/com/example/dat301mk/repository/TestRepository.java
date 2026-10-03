package com.example.dat301mk.repository;

import com.example.dat301mk.entity.Classes;
import com.example.dat301mk.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestRepository extends JpaRepository<Test, Long> {
    List<Test> findByClasses(Classes classes);
}

