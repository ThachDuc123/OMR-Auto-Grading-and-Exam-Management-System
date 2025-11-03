package com.example.dat301mk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

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

    @Column(name = "pdf_path")
    private String pdfPath;

    @Column(name = "csv_answer_path")
    private String csvAnswerPath;

    @Transient
    private Long pdfSizeKb;

    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TestVersion> testVersions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    public Long getPdfSizeKb() {
        return pdfSizeKb;
    }

    public void setPdfSizeKb(Long pdfSizeKb) {
        this.pdfSizeKb = pdfSizeKb;
    }
}
