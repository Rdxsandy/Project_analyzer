package com.codeguardian.scanservice.repository;

import com.codeguardian.scanservice.entity.ScanIssue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScanIssueRepository extends JpaRepository<ScanIssue, Long> {

    List<ScanIssue> findByScanId(Long scanId);
}
