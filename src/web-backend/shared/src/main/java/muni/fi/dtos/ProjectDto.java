package muni.fi.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import muni.fi.serializer.CustomDateSerializer;
import org.joda.time.DateTime;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Schema(description = "Project data transfer object")
public class ProjectDto extends BaseDto {

    @Schema(description = "Unique identifier of the project", example = "123")
    private String projId;

    @Schema(description = "Registration code of the project", example = "ABC-123")
    private String regCode;

    @Schema(description = "Title of the project", example = "My Project")
    private String title;

    @Schema(description = "Author data transfer object")
    private AuthorDto author;

    @Schema(description = "State of the project", example = "in_progress")
    private String state;

    @Schema(description = "Start date of the project", example = "23.04.2021")
    @JsonSerialize(using = CustomDateSerializer.class)
    private DateTime dateBegin;

    @Schema(description = "End date of the project", example = "23.04.2021")
    @JsonSerialize(using = CustomDateSerializer.class)
    private DateTime dateEnd;

    @Schema(description = "MUNI role in the project", example = "planner")
    private String muniRole;

    @Schema(description = "Investor of the project", example = "John Doe")
    private String investor;

    @Schema(description = "Department data transfer object")
    private DepartmentDto department;

    @Schema(description = "Project annotation text", example = "My project annotation")
    private String annotation;

    @JsonIgnore
    @Schema(hidden = true)
    private String processedAnnotation;

}
