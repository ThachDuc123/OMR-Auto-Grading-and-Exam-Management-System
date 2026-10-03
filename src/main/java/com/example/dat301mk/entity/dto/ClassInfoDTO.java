package com.example.dat301mk.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassInfoDTO {
    private Long classId;
    private String classCode;
    private String className;
    private String teacherName;
    private long testCount;
    private double averageScore;
}

