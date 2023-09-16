package muni.fi.bl.service;

import muni.fi.bl.ProjectLoadResult;
import muni.fi.dtos.ProjectDto;
import muni.fi.dtos.ProjectUpdateDto;
import org.springframework.data.domain.Page;

import java.io.InputStream;
import java.util.List;

public interface ProjectService {

    /**
     * Loads projects from CSV represented as an inputStream
     *
     * @param csvFile          InputStream containing CSV data
     * @param originalFilename The CSV file name
     * @return Result of the projects parsing. Contains information about the number of successfully parsed projects and other data
     */
    ProjectLoadResult loadProjectsFromCsv(InputStream csvFile, String originalFilename);

    /**
     * Used for retrieving projects. Can be paged, filtered, sorted
     *
     * @param page       Page number (starting from 0)
     * @param size       Page size
     * @param sortBy     Property to sort by. Must be a valid property-name of the ProjectDto
     * @param desc       If the sort should be descending. Otherwise ascending
     * @param title      Project 'title' filter value
     * @param regCode    Project 'regCode' filter value
     * @param uco        Project  author 'uco' filter value
     * @param department Project department 'departmentName' filter value
     * @param orgUnit    Project department 'orgUnit' filter value
     * @param muniRole   Project 'muniRole' filter value
     * @return Page of projects based on input parameters
     */
    Page<ProjectDto> searchAll(int page, int size, String sortBy, boolean desc, String title, String regCode, String uco, String department, String orgUnit, String muniRole);

    /**
     * Retrieves a project by its database id
     *
     * @param id The project database id
     * @return Found project DTO
     * @throws muni.fi.bl.exceptions.NotFoundException When project with id was not found
     */
    ProjectDto getById(Long id);

    /**
     * Returns a list of projects matched by their author uco's
     *
     * @param uco The UCO of the author whose projects you want to retrieve
     * @return The list of author's projects
     */
    List<ProjectDto> getByAuthorUco(String uco);

    /**
     * Deletes all projects
     */
    void deleteAll();

    /**
     * Deletes project by its id
     *
     * @param id The id of the project to delete
     * @throws muni.fi.bl.exceptions.NotFoundException When project with id doesn't exist
     */
    void delete(Long id);

    /**
     * Updates project with specified update DTO properties
     *
     * @param id         The id of the project to update
     * @param projectDto The update DTO containing the update info
     * @throws muni.fi.bl.exceptions.NotFoundException When project with id doesn't exist
     */
    void update(Long id, ProjectUpdateDto projectDto);

    /**
     * Returns the sample CSV content
     *
     * @return The CSV file sample content as string
     */
    String getSampleCsvContent();

}
