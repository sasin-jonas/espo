package muni.fi.query;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collections;
import java.util.List;

@Schema(description = "Search information")
public record SearchInfo(
        @Schema(description = "Maximum number of results to return in the search", example = "10") Integer maxResults,
        @Schema(description = "List of helixes to filter the search by", example = "[\"Health\", \"Digital\"]") List<String> helixes,
        @Schema(description = "List of roles to filter the search by", example = "[\"Partner\", \"Researcher\"]") List<String> roles,
        @Schema(description = "List of expertises to filter the search by", example = "[\"Java\", \"Machine learning\"]") List<String> expertises,
        @Schema(description = "List of UCOs of authors to search by", example = "[\"123456\", \"789012\"]") List<String> ucoList,
        @Schema(description = "List of IDs of projects to search by", example = "[123, 456]") List<Long> projIds,
        @Schema(description = "Indicates whether the search results should be influenced by the author recommendations") boolean personalized,
        @Schema(description = "Search phrase to use for text-based search", example = "Some search phrase") String phrase) {

    public static SearchInfo empty() {
        return new SearchInfo(null, Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, null);
    }
}
