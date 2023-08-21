package muni.fi.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@RequiredArgsConstructor
@Schema(description = "Project data transfer object")
public class ProjectEsDto extends BaseEsDto {

    @Schema(description = "Unique identifier of the project", example = "123")
    private String projId;

    @Schema(description = "Registration code of the project", example = "ABC-123")
    private String regCode;

    @Schema(description = "Title of the project", example = "My Project")
    private String title;

    @Schema(description = "Project annotation text", example = "My project annotation")
    private String description;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String uco;

}
