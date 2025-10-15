package com.example.dat301mk.repository;

import com.example.dat301mk.entity.ClassMember;
import com.example.dat301mk.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassMemberRepository extends JpaRepository<ClassMember, Long> {
    List<ClassMember> findByStudent(Users student);
}

