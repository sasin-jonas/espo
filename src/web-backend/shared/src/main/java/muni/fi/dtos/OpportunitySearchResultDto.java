package muni.fi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Search result for searching by an opportunity")
public record OpportunitySearchResultDto(AuthorDto authorDto, List<ProjectDto> relevantProjects,
                                         Double sumScore, Double averageScore, Double maxScore) {

}
