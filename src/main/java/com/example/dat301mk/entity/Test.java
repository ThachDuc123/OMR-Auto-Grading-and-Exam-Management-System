package com.example.dat301mk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "tests")
public class Test {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_id", nullable = false)
    private Long id;

    @Column(name = "test_code", nullable = false, length = 50)
    private String testCode;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "subject", length = 100)
    private String subject;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private Classes classes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Users createdBy;

    @Column(name = "created_at")
    private Instant createdAt;

    @Lob
    @Column(name = "description")
    private String description;

}
