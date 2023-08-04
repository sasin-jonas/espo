package muni.fi.bl.mappers;

import muni.fi.dal.entity.Department;
import muni.fi.dtos.DepartmentDto;
import org.mapstruct.Mapper;

@Mapper
public interface DepartmentMapper {
    DepartmentDto toDto(Department source);

    Department toEntity(DepartmentDto destination);
}
