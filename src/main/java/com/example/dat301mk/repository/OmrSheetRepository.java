package com.example.dat301mk.repository;

import com.example.dat301mk.entity.OmrSheet;
import com.example.dat301mk.entity.Test;
import com.example.dat301mk.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OmrSheetRepository extends JpaRepository<OmrSheet, Long> {
    int countByTest_Classes_IdAndStudent_Id(Long classId, Long studentId);
}
