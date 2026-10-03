package com.example.dat301mk.repository;

import com.example.dat301mk.entity.TestVersion;
import com.example.dat301mk.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestVersionRepository extends JpaRepository<TestVersion, Long> {
    int countByTest_Classes_Id(Long classId);
    List<TestVersion> findByTest(Test test);
}
