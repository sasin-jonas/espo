package muni.fi.bl.service;

import muni.fi.dtos.DepartmentDto;

import java.util.List;

public interface DepartmentService {

    /**
     * Retrieves alla available departments
     *
     * @return List of departments
     */
    List<DepartmentDto> getAll();
}
