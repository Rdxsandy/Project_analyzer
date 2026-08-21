package com.codeguardian.projectservice.repository;

import com.codeguardian.projectservice.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByRepositoryUrl(String repositoryUrl);
}
