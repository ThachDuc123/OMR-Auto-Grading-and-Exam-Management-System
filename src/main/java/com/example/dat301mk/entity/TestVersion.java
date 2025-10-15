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

    @Column(name = "file_path", length = 512)
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private Users uploadedBy;

    @Column(name = "uploaded_at")
    private Instant uploadedAt;

}
