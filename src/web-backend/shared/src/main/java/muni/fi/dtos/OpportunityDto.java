package muni.fi.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Opportunity data transfer object")
public class OpportunityDto {

    @Schema(description = "Numeric identifier of the opportunity", example = "123")
    @JsonProperty("ID")
    Integer id;

    @Schema(description = "Name of the institution offering the opportunity", example = "Harvard University")
    String institutionName;

    @Schema(description = "URL of the institution offering the opportunity", example = "https://www.harvard.edu/")
    String institutionUrl;

    @Schema(description = "URL of the opportunity appendix or supplementary information", example = "https://www.harvard.edu/opportunity/123/appendix")
    String appendixUrl;

    @Schema(description = "Title of the opportunity", example = "Quantum Computing")
    String title;

    @Schema(description = "URL of the opportunity page", example = "https://www.harvard.edu/opportunity/123")
    String url;

    @Schema(description = "Author of the opportunity", example = "John Doe")
    String author;

    @Schema(description = "Description of the opportunity", example = "Assist the principal investigator in conducting a research project on quantum computing")
    String description;

    @Schema(description = "List of research areas or helixes associated with the opportunity", example = "[\"Quantum Computing\", \"Machine Learning\"]")
    @JsonProperty("helix")
    List<String> helixes;

    @Schema(description = "List of roles associated with the opportunity", example = "[\"Research Assistant\", \"Graduate Student\"]")
    @JsonProperty("role")
    List<String> roles;

    @Schema(description = "List of expertise or skills required for the opportunity", example = "[\"Python\", \"Matlab\", \"Quantum Mechanics\"]")
    @JsonProperty("expertise")
    List<String> expertises;

    @Schema(description = "Score of the opportunity based on search criteria", example = "12.5")
    Double score;

    @Schema(description = "Based on what the opportunity was found", example = "title + description")
    String hitSource;

    @Schema(description = "Rank of the opportunity in the search results", example = "1")
    int rank;

    @Schema(description = "Unique identifier of the opportunity in the search index", example = "abcd-1234")
    String esId;

}
