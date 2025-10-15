package com.example.dat301mk.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultInfoDTO {
    private String subject;
    private String testTitle;
    private Instant submissionDate;
    private Float totalScore;
    private Float maxScore;
    private Long resultId;
}

