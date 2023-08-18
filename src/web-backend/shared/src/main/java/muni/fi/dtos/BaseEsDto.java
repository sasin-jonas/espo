package muni.fi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class BaseEsDto {
    @Schema(description = "Score of the result based on search criteria", example = "12.5")
    Double score;

    @Schema(description = "Based on what the result was found", example = "title + description")
    String hitSource;

    @Schema(description = "Rank of the result in the search results", example = "1")
    int rank;

    @Schema(description = "Unique identifier of the result in the search index", example = "abcd-1234")
    String esId;
}
