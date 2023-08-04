package muni.fi.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import muni.fi.bl.exceptions.AppException;
import muni.fi.bl.service.ProjectService;
import muni.fi.bl.service.impl.ProjectLoadResult;
import muni.fi.dtos.ProjectDto;
import muni.fi.dtos.ProjectUpdateDto;
import org.apache.commons.io.FilenameUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import static muni.fi.api.helper.ResponseHandlerHelper.writeContentToOutputStream;

@Slf4j
@RestController
@RequestMapping("/projects")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class ProjectController {

    public static final String JSON = "json";
    public static final String CSV = "csv";
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Operation(summary = "Retrieve all projects based on the filter, sort, and paging parameters")
    @GetMapping
    public Page<ProjectDto> getAll(
            @Parameter(description = "Page number (default = 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page (default = 10)")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field (optional)")
            @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort in descending order (default = false)")
            @RequestParam(defaultValue = "false") String desc,
            @Parameter(description = "Filter by project title (optional)")
            @RequestParam(required = false) String title,
            @Parameter(description = "Filter by project registration code (optional)")
            @RequestParam(required = false) String regCode,
            @Parameter(description = "Filter by author UCO (optional)")
            @RequestParam(required = false) String uco,
            @Parameter(description = "Filter by department name (optional)")
            @RequestParam(required = false) String departmentName,
            @Parameter(description = "Filter by organization unit (optional)")
            @RequestParam(required = false) String orgUnit,
            @Parameter(description = "Filter by MUNI role (optional)")
            @RequestParam(required = false) String muniRole) {
        log.info("Retrieve all projects");
        return projectService.searchAll(page, size, sortBy, Boolean.parseBoolean(desc),
                title, regCode, uco, departmentName, orgUnit, muniRole);
    }

    @Operation(summary = "Retrieve a project by ID")
    @GetMapping("/{id}")
    public ProjectDto getOne(
            @Parameter(description = "ID of the project to retrieve")
            @PathVariable("id") Long id) {
        log.info("Get project with id {}", id);
        return projectService.getById(id);
    }

    @Operation(summary = "Delete a project by ID")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(
            @Parameter(description = "ID of the project to delete")
            @PathVariable("id") Long id) {
        log.info("Deleting project with id {}", id);
        projectService.delete(id);
    }

    @Operation(summary = "Update a project by ID")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public void updateProject(
            @Parameter(description = "ID of the project to update")
            @PathVariable Long id,
            @Parameter(description = "Updated properties for the project")
            @RequestBody ProjectUpdateDto projectDto) {
        log.info("Updating project with id {} and with updated properties: {}", id, projectDto);
        projectService.update(id, projectDto);
    }

    @Operation(summary = "Retrieve projects by author's UCO")
    @GetMapping("/author/{uco}")
    public List<ProjectDto> getByAuthor(
            @Parameter(description = "UCO of the author to retrieve projects for")
            @PathVariable("uco") String uco) {
        log.info("Getting projects for author with uco {}", uco);
        return projectService.getByAuthorUco(uco);
    }

    @Operation(summary = "Upload new MU projects from a file")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/load", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String loadNew(
            @Parameter(description = "File containing MU projects to upload")
            @RequestParam("file") MultipartFile importFile) {
        log.info("Uploading MU projects from file {}", importFile.getOriginalFilename());
        String fileExtension = FilenameUtils.getExtension(importFile.getOriginalFilename());
        InputStream inputStream = getInputStream(importFile);
        return upload(fileExtension, inputStream);
    }

    @Operation(summary = "Upload and replace all existing MU projects from a file")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @PostMapping(path = "/load-all", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String loadAll(
            @Parameter(description = "File containing MU projects to upload")
            @RequestParam("file") MultipartFile importFile) {
        log.info("Uploading and replacing MU projects from file {}", importFile.getOriginalFilename());
        String fileExtension = FilenameUtils.getExtension(importFile.getOriginalFilename());
        InputStream inputStream = getInputStream(importFile);
        projectService.deleteAll();
        return upload(fileExtension, inputStream);
    }

    @Operation(summary = "Download an example CSV file for MU projects")
    @GetMapping("/example-csv")
    public void downloadExampleCsv(HttpServletResponse response) {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=example.csv");

        String sampleContent = projectService.getSampleCsvContent();
        writeContentToOutputStream(response, sampleContent);
    }

    @Operation(summary = "Download an example JSON file for MU projects")
    @GetMapping("/example-json")
    public void downloadExampleJson(HttpServletResponse response) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Content-Disposition", "attachment; filename=example.json");

        String sampleContent = projectService.getSampleJsonContent();
        writeContentToOutputStream(response, sampleContent);
    }

    private InputStream getInputStream(MultipartFile importFile) {
        InputStream inputStream;
        try {
            inputStream = importFile.getInputStream();
        } catch (IOException e) {
            String message = "Error while reading projects upload file";
            log.error(message, e);
            throw new AppException(message, e);
        }
        return inputStream;
    }

    private String upload(String fileExtension, InputStream inputStream) {
        if (Objects.equals(fileExtension, JSON)) {
            return getSuccessMessage(projectService.loadProjectsFromJson(inputStream));
        } else if (Objects.equals(fileExtension, CSV)) {
            return getSuccessMessage(projectService.loadProjectsFromCsv(inputStream));
        } else {
            String message = "Invalid file extension, please use csv or json";
            log.warn(message);
            throw new AppException(message);
        }
    }


    private String getSuccessMessage(ProjectLoadResult projectsUploadedFile) {
        return String.format("Successfully loaded %d/%d project records (%d failed)",
                projectsUploadedFile.successful(), projectsUploadedFile.total(), projectsUploadedFile.failed());
    }

}
