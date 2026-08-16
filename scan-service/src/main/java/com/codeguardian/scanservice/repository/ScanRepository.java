package com.codeguardian.scanservice.repository;

import com.codeguardian.scanservice.entity.Scan;
import com.codeguardian.scanservice.entity.ScanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScanRepository extends JpaRepository<Scan, Long> {

    List<Scan> findByProjectId(Long projectId);

    List<Scan> findByStatus(ScanStatus status);
}
