package com.example.dat301mk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "results")
public class Result {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sheet_id", nullable = false)
    private OmrSheet sheet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Users student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @Column(name = "total_correct")
    private Integer totalCorrect;

    @Column(name = "total_wrong")
    private Integer totalWrong;

    @Column(name = "total_blank")
    private Integer totalBlank;

    @Column(name = "total_score")
    private Float totalScore;

    @Column(name = "max_score")
    private Float maxScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "graded_by")
    private Users gradedBy;

    @Column(name = "graded_at")
    private Instant gradedAt;

    @Column(name = "published")
    private Boolean published;

}
