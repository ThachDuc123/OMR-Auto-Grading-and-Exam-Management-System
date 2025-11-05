package com.example.dat301mk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "test_versions")
public class TestVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "version_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @Column(name = "version_code", nullable = false, length = 50)
    private String versionCode;

    @Column(name = "is_answer_key", nullable = false)
    private Boolean isAnswerKey = false;

    // New: preferred modern column for PDF path
    @Column(name = "file_path", length = 512)
    private String filePath;

    // New: legacy columns existing in sample DB
    @Column(name = "pdf_path", length = 512)
    private String pdfPath; // legacy support

    @Column(name = "csv_answer_path", length = 512)
    private String csvAnswerPath; // legacy support for answers

    // New: modern column for answer path
    @Column(name = "answer_path", length = 512)
    private String answerPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private Users uploadedBy;

    @Column(name = "uploaded_at")
    private Instant uploadedAt;
}
