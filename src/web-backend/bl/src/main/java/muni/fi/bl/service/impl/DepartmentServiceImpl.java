package muni.fi.bl.service.impl;

import muni.fi.bl.mappers.DepartmentMapper;
import muni.fi.bl.service.DepartmentService;
import muni.fi.dal.repository.DepartmentRepository;
import muni.fi.dtos.DepartmentDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository, DepartmentMapper departmentMapper) {
        this.departmentRepository = departmentRepository;
        this.departmentMapper = departmentMapper;
    }

    @Override
    public List<DepartmentDto> getAll() {
        return departmentRepository.findAll().stream()
                .map(departmentMapper::toDto)
                .toList();
    }
}
