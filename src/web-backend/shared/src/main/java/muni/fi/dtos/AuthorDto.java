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
@Schema(description = "Project author")
public class AuthorDto extends BaseDto {

    @Schema(description = "Name of the author", example = "John Doe")
    private String name;

    @Schema(description = "UCO (unique identifier) of the author", example = "123456")
    private String uco;

    @Schema(description = "Type of the author", example = "Employee")
    private String type;

}
