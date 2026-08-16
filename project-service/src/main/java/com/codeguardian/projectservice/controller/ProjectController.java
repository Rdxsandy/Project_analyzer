package com.codeguardian.projectservice.controller;

import com.codeguardian.projectservice.dto.ProjectRequest;
import com.codeguardian.projectservice.dto.ProjectResponse;
import com.codeguardian.projectservice.entity.Project;
import com.codeguardian.projectservice.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectRequest request) {

        Project project = new Project();

        project.setName(request.getName());
        project.setRepositoryUrl(request.getRepositoryUrl());
        project.setDescription(request.getDescription());
        project.setLanguage(request.getLanguage());
        project.setDefaultBranch(request.getDefaultBranch());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(projectService.createProject(project));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        return ResponseEntity.ok(
                projectService.getAllProjects()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                projectService.getProjectById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request) {

        Project project = new Project();

        project.setName(request.getName());
        project.setRepositoryUrl(request.getRepositoryUrl());
        project.setDescription(request.getDescription());
        project.setLanguage(request.getLanguage());
        project.setDefaultBranch(request.getDefaultBranch());

        return ResponseEntity.ok(
                projectService.updateProject(id, project)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long id) {

        projectService.deleteProject(id);

        return ResponseEntity.noContent().build();
    }
}
