package muni.fi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@Schema(description = "Base data transfer object")
public abstract class BaseDto {

    @Schema(description = "Unique identifier of the entity", example = "123")
    private Long id;
}
