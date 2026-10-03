package com.example.dat301mk.repository;

import com.example.dat301mk.entity.Result;
import com.example.dat301mk.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultRepository extends JpaRepository<Result, Long> {
    List<Result> findByStudent(Users student);
}

