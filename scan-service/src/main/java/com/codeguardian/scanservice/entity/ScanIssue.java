package com.codeguardian.scanservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "scan_issues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScanIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long scanId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueType type;

    @Column(nullable = false, length = 2000)
    private String message;

    private String filePath;

    private Integer lineNumber;

    private String ruleId;

    private String suggestion;
}
