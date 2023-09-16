package muni.fi.bl.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import co.elastic.clients.elasticsearch.core.DeleteByQueryRequest;
import co.elastic.clients.elasticsearch.core.DeleteByQueryResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import lombok.extern.slf4j.Slf4j;
import muni.fi.bl.ProjectLoadResult;
import muni.fi.bl.component.ElasticLoaderAccessor;
import muni.fi.bl.component.ProjectParser;
import muni.fi.bl.exceptions.AppException;
import muni.fi.bl.exceptions.ConnectionException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static muni.fi.bl.exceptions.ConnectionException.ELASTIC_CONNECTION_ERROR;
import static muni.fi.bl.service.impl.ElasticSearchService.MU_INDEX;

@Service
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    public static final String UPLOAD_ENDPOINT = "/loadMuProjects";
    public static final String PROJ_ID_FIELD = "projId";

    private final ProjectRepository projectRepository;
    private final AuthorRepository authorRepository;
    private final DepartmentRepository departmentRepository;
    private final ProjectMapper projectMapper;
    private final ProjectParser csvParser;
    private final ElasticLoaderAccessor elasticLoaderAccessor;
    private final ElasticsearchClient elasticsearchClient;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository,
                              AuthorRepository authorRepository,
                              DepartmentRepository departmentRepository,
                              ProjectMapper projectMapper,
                              ProjectParser csvParser,
                              ElasticLoaderAccessor elasticLoaderAccessor,
                              ElasticsearchClient elasticsearchClient) {
        this.projectRepository = projectRepository;
        this.authorRepository = authorRepository;
        this.departmentRepository = departmentRepository;
        this.projectMapper = projectMapper;
        this.csvParser = csvParser;
        this.elasticLoaderAccessor = elasticLoaderAccessor;
        this.elasticsearchClient = elasticsearchClient;
    }

    @Override
    @Transactional
    public ProjectLoadResult loadProjectsFromCsv(InputStream stream, String originalFilename) {
        log.info("Loading projects from csv");

        return loadProjects(csvParser, stream, originalFilename);
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
    @Transactional
    public void deleteAll() {
        log.info("Deleting all projects");
        projectRepository.deleteAll();

        DeleteIndexRequest deleteRequest = DeleteIndexRequest.of(b -> b
                .index(MU_INDEX)
                .allowNoIndices(true)
                .ignoreUnavailable(true));
        try {
            elasticsearchClient.indices().delete(deleteRequest);
        } catch (IOException e) {
            log.error(ELASTIC_CONNECTION_ERROR, e);
            throw new ConnectionException(ELASTIC_CONNECTION_ERROR, e);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Optional<Project> project = projectRepository.findById(id);
        if (project.isEmpty()) {
            String message = String.format("Project with id %d doesn't exist", id);
            log.warn(message);
            throw new NotFoundException(message);
        }
        String projId = project.get().getProjId();
        projectRepository.deleteById(id);
        deleteProjectByIdInElastic(projId);
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

    private ProjectLoadResult loadProjects(ProjectParser parser, InputStream stream, String originalFilename) {
        ByteArrayOutputStream copyStream = new ByteArrayOutputStream();
        try {
            stream.transferTo(copyStream);
            stream.close();
        } catch (IOException e) {
            log.info("Failed to read CSV data", e);
            throw new AppException("Failed to read CSV data", e);
        }
        InputStream projectsStreamCopy = new ByteArrayInputStream(copyStream.toByteArray());
        ProjectLoadResult result = parser.parseProjects(projectsStreamCopy);
        for (Project project : result.projects()) {
            setProjectDetails(project);
            projectRepository.save(project);
        }
        elasticLoaderAccessor.sendDataToElasticLoader(originalFilename, copyStream.toByteArray(), UPLOAD_ENDPOINT);
        try {
            copyStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to close input stream", e);
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

    private void deleteProjectByIdInElastic(String projId) {
        // Create a DeleteByQueryRequest to delete documents by 'projId' field.
        TermsQuery.Builder termsQueryBuilder = new TermsQuery.Builder();
        TermsQueryField termsQueryField = TermsQueryField.of(t -> t
                .value(List.of(FieldValue.of(projId))));
        termsQueryBuilder
                .field(PROJ_ID_FIELD)
                .terms(termsQueryField);
        DeleteByQueryRequest deleteByQueryRequest = DeleteByQueryRequest.of(q ->
                q.index(MU_INDEX)
                        .query(
                                termsQueryBuilder.build()._toQuery()
                        ));
        try {
            // Perform delete by query operation.
            DeleteByQueryResponse deleteByQueryResponse = elasticsearchClient.deleteByQuery(deleteByQueryRequest);

            // Check the response status.
            if (deleteByQueryResponse.deleted() != null && deleteByQueryResponse.deleted() != 1) {
                String message = String.format("Couldn't delete documents with projId '%s'", projId);
                log.warn(message);
                throw new NotFoundException(message);
            }
        } catch (IOException e) {
            log.error(ELASTIC_CONNECTION_ERROR, e);
            throw new ConnectionException(ELASTIC_CONNECTION_ERROR, e);
        }
    }
}
