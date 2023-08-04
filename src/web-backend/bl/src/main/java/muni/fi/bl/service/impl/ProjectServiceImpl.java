package muni.fi.bl.service.impl;

import lombok.extern.slf4j.Slf4j;
import muni.fi.bl.component.ProjectParser;
import muni.fi.bl.exceptions.NotFoundException;
import muni.fi.bl.mappers.ProjectMapper;
import muni.fi.bl.service.ProjectService;
import muni.fi.dal.entity.Author;
import muni.fi.dal.entity.Department;
import muni.fi.dal.entity.Project;
import muni.fi.dal.repository.AuthorRepository;
import muni.fi.dal.repository.DepartmentRepository;
import muni.fi.dal.repository.ProjectRepository;
import muni.fi.dal.specification.ProjectSpecifications;
import muni.fi.dtos.ProjectDto;
import muni.fi.dtos.ProjectUpdateDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final AuthorRepository authorRepository;
    private final DepartmentRepository departmentRepository;
    private final ProjectMapper projectMapper;
    private final ProjectParser csvParser;
    private final ProjectParser jsonParser;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository,
                              AuthorRepository authorRepository,
                              DepartmentRepository departmentRepository,
                              ProjectMapper projectMapper,
                              @Qualifier("csvParser") ProjectParser csvParser,
                              @Qualifier("jsonParser") ProjectParser jsonParser) {
        this.projectRepository = projectRepository;
        this.authorRepository = authorRepository;
        this.departmentRepository = departmentRepository;
        this.projectMapper = projectMapper;
        this.jsonParser = jsonParser;
        this.csvParser = csvParser;
    }

    @Override
    @Transactional
    public ProjectLoadResult loadProjectsFromCsv(InputStream stream) {
        log.info("Loading projects from csv");

        return loadProjects(csvParser, stream);
    }

    @Override
    @Transactional
    public ProjectLoadResult loadProjectsFromJson(InputStream jsonFile) {
        log.info("Loading projects from json");

        return loadProjects(jsonParser, jsonFile);
    }

    @Override
    public Page<ProjectDto> searchAll(int page, int size, String sortBy,
                                      boolean desc, String title, String regCode, String uco, String department, String orgUnit, String muniRole) {
        Sort by = Sort.unsorted();
        if (sortBy != null) {
            by = Sort.by(sortBy);
            if (desc) {
                by = by.descending();
            }
        }
        Pageable pageRequest = PageRequest.of(page, size, by);
        Specification<Project> specification = Specification.where(null);
        if (!StringUtils.isBlank(title)) {
            specification = specification.and(ProjectSpecifications.hasTitleContaining(title));
        }
        if (!StringUtils.isBlank(regCode)) {
            specification = specification.and(ProjectSpecifications.hasRegCodeContaining(regCode));
        }
        if (!StringUtils.isBlank(muniRole)) {
            specification = specification.and(ProjectSpecifications.hasMuniRoleContaining(muniRole));
        }
        if (!StringUtils.isBlank(uco)) {
            specification = specification.and(ProjectSpecifications.hasAuthorUcoContaining(uco));
        }
        if (!StringUtils.isBlank(department)) {
            specification = specification.and(ProjectSpecifications.hasDepartmentNameContaining(department));
        }
        if (!StringUtils.isBlank(orgUnit)) {
            specification = specification.and(ProjectSpecifications.hasDepartmentOrgUnitContaining(orgUnit));
        }
        return projectRepository.findAll(specification, pageRequest)
                .map(projectMapper::toDto);
    }

    @Override
    public ProjectDto getById(Long id) {
        var project = projectRepository.findById(id);
        if (project.isEmpty()) {
            String message = String.format("Project with id %d not found", id);
            log.info(message);
            throw new NotFoundException(message);
        }
        return projectMapper.toDto(project.get());
    }

    @Override
    public List<ProjectDto> getByAuthorUco(String uco) {
        var projects = projectRepository.findByAuthorUco(uco);
        return projectMapper.toDtos(projects);
    }

    @Override
    public void deleteAll() {
        log.info("Deleting all projects");
        projectRepository.deleteAll();
    }

    @Override
    public void delete(Long id) {
        Optional<Project> project = projectRepository.findById(id);
        if (project.isEmpty()) {
            String message = String.format("Project with id %d doesn't exist", id);
            log.warn(message);
            throw new NotFoundException(message);
        }
        projectRepository.deleteById(id);
    }

    @Override
    public void update(Long id, ProjectUpdateDto updateDto) {
        Optional<Project> project = projectRepository.findById(id);
        if (project.isEmpty()) {
            String message = String.format("Project with id %d doesn't exist", id);
            log.warn(message);
            throw new NotFoundException(message);
        }
        ProjectDto projDto = projectMapper.toDto(project.get());
        projDto.setProjId(updateDto.getProjId());
        projDto.setRegCode(updateDto.getRegCode());
        projDto.setTitle(updateDto.getTitle());
        projDto.setAuthor(updateDto.getAuthor());
        projDto.setMuniRole(updateDto.getMuniRole());
        projDto.setDepartment(updateDto.getDepartment());
        projDto.setAnnotation(updateDto.getAnnotation());
        projectRepository.save(projectMapper.toEntity(projDto));
    }

    @Override
    public String getSampleCsvContent() {
        return csvParser.getSample();
    }

    @Override
    public String getSampleJsonContent() {
        return jsonParser.getSample();
    }

    private ProjectLoadResult loadProjects(ProjectParser parser, InputStream stream) {
        ProjectLoadResult result = parser.parseProjects(stream);
        for (Project project : result.projects()) {
            setProjectDetails(project);
            projectRepository.save(project);
        }
        return result;
    }

    private void setProjectDetails(Project project) {
        Optional<Author> author = authorRepository.findByUco(project.getAuthor().getUco());
        Optional<Department> department = departmentRepository.findByOrgUnitAndDepartmentName(
                project.getDepartment().getOrgUnit(), project.getDepartment().getDepartmentName());

        author.ifPresent(project::setAuthor);
        department.ifPresent(project::setDepartment);
    }
}
