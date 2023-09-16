package muni.fi.bl.service.impl;

import muni.fi.bl.ProjectLoadResult;
import muni.fi.bl.component.ElasticLoaderAccessor;
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
import muni.fi.dtos.AuthorDto;
import muni.fi.dtos.DepartmentDto;
import muni.fi.dtos.ProjectDto;
import muni.fi.dtos.ProjectUpdateDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class ProjectServiceImplTest {

    public static final String DUMMY_FILENAME = "fileName";

    @Mock
    private ProjectRepository projectRepositoryMock;
    @Mock
    private AuthorRepository authorRepositoryMock;
    @Mock
    private DepartmentRepository departmentRepositoryMock;
    @Mock
    private ProjectParser csvParserMock;
    @Mock
    private ProjectMapper projectMapperMock;
    @Mock
    private ElasticLoaderAccessor elasticLoaderAccessor;

    @Captor
    private ArgumentCaptor<Specification<Project>> specificationCaptor;
    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    private ProjectLoadResult loadResult;

    // tested class
    private ProjectService projectService;
    private Project project1;
    private Project project2;

    @BeforeEach
    void setUp() {
        openMocks(this);

        projectService = new ProjectServiceImpl(projectRepositoryMock, authorRepositoryMock, departmentRepositoryMock,
                Mappers.getMapper(ProjectMapper.class), csvParserMock, elasticLoaderAccessor);

        Author author1 = new Author("John Doe", "123456", "student");
        Author author2 = new Author("Jenna Doe", "654321", "employee");
        Department department1 = new Department("LF", "department1");
        Department department2 = new Department("FI", "department2");
        project1 = new Project();
        project2 = new Project();
        project1.setAuthor(author1);
        project2.setAuthor(author2);
        project1.setDepartment(department1);
        project2.setDepartment(department2);
        loadResult = new ProjectLoadResult(2, 1, 1, List.of(project1, project2));

        Author authorReturned1 = new Author("John Doe", "123456", "student");
        Author authorReturned2 = new Author("Jenna Doe", "654321", "employee");
        authorReturned1.setId(1L);
        authorReturned2.setId(2L);
        Department departmentReturned1 = new Department("LF", "department1");
        Department departmentReturned2 = new Department("FI", "department2");
        departmentReturned1.setId(1L);
        departmentReturned2.setId(2L);
        when(authorRepositoryMock.findByUco(author1.getUco())).thenReturn(Optional.of(authorReturned1));
        when(authorRepositoryMock.findByUco(author2.getUco())).thenReturn(Optional.of(authorReturned2));
        when(departmentRepositoryMock.findByOrgUnitAndDepartmentName(department1.getOrgUnit(), department1.getDepartmentName()))
                .thenReturn(Optional.of(departmentReturned1));
        when(departmentRepositoryMock.findByOrgUnitAndDepartmentName(department2.getOrgUnit(), department2.getDepartmentName()))
                .thenReturn(Optional.of(departmentReturned2));
    }

    @Test
    void loadProjectsFromCsv() {
        // prepare
        when(csvParserMock.parseProjects(any())).thenReturn(loadResult);

        // tested method
        ProjectLoadResult result = projectService.loadProjectsFromCsv(InputStream.nullInputStream(), DUMMY_FILENAME);

        // verify
        assertThat(result, equalTo(loadResult));
        verify(csvParserMock).parseProjects(any());
        verify(authorRepositoryMock, times(2)).findByUco(any());
        verify(departmentRepositoryMock, times(2)).findByOrgUnitAndDepartmentName(any(), any());

        assertThat(result.projects().get(0).getAuthor().getId(), equalTo(1L));
        assertThat(result.projects().get(1).getAuthor().getId(), equalTo(2L));
        assertThat(result.projects().get(0).getDepartment().getId(), equalTo(1L));
        assertThat(result.projects().get(1).getDepartment().getId(), equalTo(2L));
    }

    @Test
    void getAll() {
        // prepare
        when(projectRepositoryMock.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(project1, project2)));

        // tested method
        Page<ProjectDto> result = projectService.searchAll(0, 10, "uco", true, "title", "regCode", "123456",
                "department", "LF", "partner");

        // verify
        assertThat(result.getTotalElements(), equalTo(2L));

        verify(projectRepositoryMock).findAll(specificationCaptor.capture(), pageableCaptor.capture());

        assertThat(pageableCaptor.getValue().getOffset(), equalTo(0L));
        assertThat(pageableCaptor.getValue().getPageNumber(), equalTo(0));
        assertThat(pageableCaptor.getValue().getPageSize(), equalTo(10));
        Sort.Order order = Objects.requireNonNull(pageableCaptor.getValue().getSort().getOrderFor("uco"));
        assertThat(order.getDirection(), equalTo(Sort.Direction.DESC));
        assertThat(order.getProperty(), equalTo("uco"));

        List<Specification<Project>> specifications = specificationCaptor.getAllValues();
        assertThat(specifications.size(), equalTo(1));
    }

    @Test
    void getById() {
        // prepare
        projectService = new ProjectServiceImpl(projectRepositoryMock, authorRepositoryMock, departmentRepositoryMock,
                projectMapperMock, csvParserMock, elasticLoaderAccessor);
        when(projectRepositoryMock.findById(eq(1L))).thenReturn(Optional.of(project1));

        // tested method
        projectService.getById(1L);

        // verify
        verify(projectRepositoryMock).findById(eq(1L));
        verify(projectMapperMock).toDto(project1);
    }

    @Test
    void getByIdNotFound() {
        // prepare
        when(projectRepositoryMock.findById(eq(1L))).thenReturn(Optional.empty());

        // tested method
        Throwable exception = assertThrows(NotFoundException.class, () -> projectService.getById(1L));

        // verify
        assertThat(exception.getMessage(), equalTo("Project with id 1 not found"));
    }

    @Test
    void getByAuthorUco() {
        // prepare
        projectService = new ProjectServiceImpl(projectRepositoryMock, authorRepositoryMock, departmentRepositoryMock,
                projectMapperMock, csvParserMock, elasticLoaderAccessor);
        List<Project> projects = List.of(this.project1, project2);
        when(projectRepositoryMock.findByAuthorUco(eq("uco"))).thenReturn(projects);

        // tested method
        projectService.getByAuthorUco("uco");

        // verify
        verify(projectRepositoryMock).findByAuthorUco("uco");
        verify(projectMapperMock).toDtos(projects);
    }

    @Test
    void deleteAll() {
        // tested method
        projectService.deleteAll();

        // verify
        verify(projectRepositoryMock).deleteAll();
    }

    @Test
    void delete() {
        // prepare
        when(projectRepositoryMock.findById(1L)).thenReturn(Optional.of(project1));

        // tested method
        projectService.delete(1L);

        // verify
        verify(projectRepositoryMock).findById(1L);
        verify(projectRepositoryMock).deleteById(1L);
    }

    @Test
    void deleteNotFound() {
        // prepare
        when(projectRepositoryMock.findById(1L)).thenReturn(Optional.empty());

        // tested method
        Throwable exception = assertThrows(NotFoundException.class, () -> projectService.delete(1L));

        // verify
        assertThat(exception.getMessage(), equalTo("Project with id 1 doesn't exist"));
    }

    @Test
    void update() {
        // prepare
        when(projectRepositoryMock.findById(1L)).thenReturn(Optional.of(project1));
        ProjectDto dto = new ProjectDto();
        Project entity = new Project();
        when(projectMapperMock.toDto(any())).thenReturn(dto);
        when(projectMapperMock.toEntity(any())).thenReturn(entity);
        projectService = new ProjectServiceImpl(projectRepositoryMock, authorRepositoryMock, departmentRepositoryMock,
                projectMapperMock, csvParserMock, elasticLoaderAccessor);
        ProjectUpdateDto updateDto = new ProjectUpdateDto("id", "regCode", "title", new AuthorDto(),
                "role", new DepartmentDto(), "annotation");

        // tested method
        projectService.update(1L, updateDto);

        // verify
        verify(projectRepositoryMock).findById(1L);
        verify(projectRepositoryMock).save(entity);
        verify(projectMapperMock).toDto(project1);
        verify(projectMapperMock).toEntity(dto);
    }

    @Test
    void updateNotFound() {
        // prepare
        when(projectRepositoryMock.findById(1L)).thenReturn(Optional.empty());
        ProjectUpdateDto updateDto = new ProjectUpdateDto("id", "regCode", "title", new AuthorDto(),
                "role", new DepartmentDto(), "annotation");

        // tested method
        Throwable exception = assertThrows(NotFoundException.class, () -> projectService.update(1L, updateDto));

        // verify
        assertThat(exception.getMessage(), equalTo("Project with id 1 doesn't exist"));
    }

    @Test
    void getSampleCsvContent() {
        // tested method
        projectService.getSampleCsvContent();

        // verify
        verify(csvParserMock).getSample();
    }

}