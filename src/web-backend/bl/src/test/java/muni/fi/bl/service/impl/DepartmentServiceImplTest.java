package muni.fi.bl.service.impl;

import muni.fi.bl.mappers.DepartmentMapper;
import muni.fi.bl.service.DepartmentService;
import muni.fi.dal.entity.Department;
import muni.fi.dal.repository.DepartmentRepository;
import muni.fi.dtos.DepartmentDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository DepartmentRepositoryMock;
    // tested class
    private DepartmentService DepartmentService;

    @BeforeEach
    void setUp() {
        openMocks(this);

        // mappers are not mocked as the implementation is not unit-tested because it is automatically generated by mapStruct
        DepartmentMapper mapper = Mappers.getMapper(DepartmentMapper.class);
        DepartmentService = new DepartmentServiceImpl(DepartmentRepositoryMock, mapper);
    }

    @Test
    void getAll() {
        // prepare
        Department Department1 = new Department("LF", "department 1");
        Department Department2 = new Department("FI", "department 2");
        when(DepartmentRepositoryMock.findAll()).thenReturn(List.of(Department1, Department2));

        // tested method
        List<DepartmentDto> DepartmentDtos = DepartmentService.getAll();

        // verify
        assertThat(DepartmentDtos.size(), equalTo(2));
        assertThat(DepartmentDtos.get(0).getOrgUnit(), equalTo("LF"));
        assertThat(DepartmentDtos.get(0).getDepartmentName(), equalTo("department 1"));
        assertThat(DepartmentDtos.get(1).getOrgUnit(), equalTo("FI"));
        assertThat(DepartmentDtos.get(1).getDepartmentName(), equalTo("department 2"));
    }
}