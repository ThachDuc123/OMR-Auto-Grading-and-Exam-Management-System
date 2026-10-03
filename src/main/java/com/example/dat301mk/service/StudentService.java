package com.example.dat301mk.service;

import com.example.dat301mk.entity.dto.ClassInfoDTO;
import com.example.dat301mk.entity.dto.ResultInfoDTO;

import java.util.List;

public interface StudentService {
    List<ClassInfoDTO> getClassInfoForStudent(String username);
    List<ResultInfoDTO> getRecentResultsForStudent(String username, int limit);
}
