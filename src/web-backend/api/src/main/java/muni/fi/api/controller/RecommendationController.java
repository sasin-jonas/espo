package muni.fi.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import muni.fi.bl.service.RecommendationService;
import muni.fi.dtos.OpportunityDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@RequestMapping("/recommend")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @Operation(summary = "Recommend similar opportunities based on the given opportunity ID")
    @GetMapping("/{id}")
    public List<OpportunityDto> recommendMoreLikeThis(
            @Parameter(description = "The Elasticsearch ID of the opportunity") @PathVariable("id") String id) {
        log.info("Recommending similar opportunities for opportunity with id {}", id);
        return recommendationService.recommendMoreLikeThis(id);
    }
}
