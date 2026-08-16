package com.codeguardian.projectservice.service;

import com.codeguardian.projectservice.dto.ProjectResponse;
import com.codeguardian.projectservice.entity.Project;
import com.codeguardian.projectservice.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public ProjectResponse createProject(Project project) {
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        Project savedProject = projectRepository.save(project);

        return toResponse(savedProject);
    }

    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProjectResponse getProjectById(Long id) {
        return toResponse(getProjectEntityById(id));
    }

    public ProjectResponse updateProject(Long id, Project updatedProject) {

        Project project = getProjectEntityById(id);

        project.setName(updatedProject.getName());
        project.setRepositoryUrl(updatedProject.getRepositoryUrl());
        project.setDescription(updatedProject.getDescription());
        project.setLanguage(updatedProject.getLanguage());
        project.setDefaultBranch(updatedProject.getDefaultBranch());
        project.setUpdatedAt(LocalDateTime.now());

        Project savedProject = projectRepository.save(project);

        return toResponse(savedProject);
    }

    public void deleteProject(Long id) {
        Project project = getProjectEntityById(id);
        projectRepository.delete(project);
    }

    private Project getProjectEntityById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }

    private ProjectResponse toResponse(Project project) {

        ProjectResponse response = new ProjectResponse();

        response.setId(project.getId());
        response.setName(project.getName());
        response.setRepositoryUrl(project.getRepositoryUrl());
        response.setDescription(project.getDescription());
        response.setLanguage(project.getLanguage());
        response.setDefaultBranch(project.getDefaultBranch());
        response.setCreatedAt(project.getCreatedAt());
        response.setUpdatedAt(project.getUpdatedAt());

        return response;
    }
}
