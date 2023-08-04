package muni.fi.bl.mappers;

import muni.fi.dal.entity.Project;
import muni.fi.dtos.ProjectDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface ProjectMapper {
    ProjectDto toDto(Project source);

    Project toEntity(ProjectDto destination);

    List<ProjectDto> toDtos(List<Project> sources);
}
