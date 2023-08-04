package muni.fi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Schema(description = "Project update object")
public class ProjectUpdateDto extends BaseDto {

    @Schema(description = "Unique identifier of the project to be updated", example = "123")
    private String projId;

    @Schema(description = "Registration code of the project", example = "ABC-123")
    private String regCode;

    @Schema(description = "Title of the project", example = "My Project")
    private String title;

    @Schema(description = "Author data transfer object")
    private AuthorDto author;

    @Schema(description = "Municipality role of the project", example = "planner")
    private String muniRole;

    @Schema(description = "Department data transfer object")
    private DepartmentDto department;

    @Schema(description = "Annotation of the project", example = "My project annotation")
    private String annotation;

}
