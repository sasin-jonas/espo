package muni.fi.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import muni.fi.bl.service.AggregationService;
import muni.fi.bl.service.SearchService;
import muni.fi.dtos.OpportunityDto;
import muni.fi.query.SearchInfo;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/search")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class SearchController {

    private final SearchService searchService;
    private final AggregationService aggregationService;

    public SearchController(SearchService searchService,
                            AggregationService aggregationService) {
        this.searchService = searchService;
        this.aggregationService = aggregationService;
    }

    @GetMapping("/all")
    @Operation(summary = "Search for all opportunities based on the filter, sort, and paging parameters")
    public Page<OpportunityDto> searchAll(
            @Parameter(description = "Page number (default = 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page (default = 10)")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field (optional)")
            @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort in descending order (default = false)")
            @RequestParam(defaultValue = "false") Boolean desc,
            @Parameter(description = "Filter field (optional)")
            @RequestParam(required = false) String filterField,
            @Parameter(description = "Filter value (optional)")
            @RequestParam(required = false) String filterValue) {
        log.info("Searching for all opportunities");
        return searchService.searchForAll(page, size, sortBy, desc, filterField, filterValue);
    }

    @Operation(summary = "Search for opportunities by projects")
    @PostMapping("/byProjects")
    public List<OpportunityDto> searchByProjects(
            @Parameter(description = "Search information")
            @RequestBody SearchInfo info) {
        log.info("Searching for opportunities by projects with search info: {}", info);
        return searchService.searchByProjects(info);
    }

    @Operation(summary = "Search for opportunities by authors")
    @PostMapping("/byAuthors")
    public List<OpportunityDto> searchByAuthors(
            @Parameter(description = "Search information")
            @RequestBody SearchInfo info) {
        log.info("Searching for opportunities by author with search info: {}", info);
        return searchService.searchByAuthors(info);
    }

    @Operation(summary = "Search for opportunities by search phrase")
    @PostMapping("/byPhrase")
    public List<OpportunityDto> searchByPhrase(
            @Parameter(description = "Search information")
            @RequestBody SearchInfo info) {
        log.info("Searching for opportunities by search phrase with search info: {}", info);
        return searchService.searchByPhrase(info);
    }

    @Operation(summary = "Get unique filters")
    @GetMapping("/unique-filters")
    public Map<String, List<String>> aggUniqueTerms() {
        log.info("Searching for unique filters map");
        return aggregationService.searchUniqueAggAll();
    }
}
