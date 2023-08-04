package muni.fi.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import muni.fi.bl.service.OpportunityService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static muni.fi.api.helper.ResponseHandlerHelper.writeContentToOutputStream;

@Slf4j
@RestController
@RequestMapping("/opportunities")
@PreAuthorize("hasRole('ADMIN')")
public class OpportunitiesController {

    private final OpportunityService opportunityService;

    public OpportunitiesController(OpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @Operation(summary = "Delete an opportunity by ID")
    @DeleteMapping("/{id}")
    public void delete(
            @Parameter(description = "ID of the opportunity to be deleted")
            @PathVariable("id") String id) {
        log.info("Deleting opportunity with id {}", id);
        opportunityService.delete(id);
    }

    @Operation(summary = "Load new opportunities from a CSV file")
    @PostMapping(path = "/load", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String loadNew(
            @Parameter(description = "CSV file containing new opportunities to be loaded")
            @RequestParam("file") MultipartFile importFile) throws IOException {
        log.info("Loading new crowdHelix opportunities");

        return opportunityService.load(importFile.getOriginalFilename(), importFile.getBytes());
    }

    @Operation(summary = "Load and replace all existing opportunities with new ones from a CSV file")
    @PostMapping(path = "/load-all", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String loadAndReplace(
            @Parameter(description = "CSV file containing new opportunities to be loaded and replace all existing ones")
            @RequestParam("file") MultipartFile importFile) throws IOException {
        log.info("Loading new and replacing old crowdHelix opportunities");

        opportunityService.deleteAll();
        return opportunityService.load(importFile.getOriginalFilename(), importFile.getBytes());
    }

    @Operation(summary = "Download an example CSV file containing the opportunity schema")
    @GetMapping("/example-csv")
    public void downloadExampleCsv(HttpServletResponse response) {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=example.csv");

        String sampleContent = opportunityService.getSampleCsvContent();
        writeContentToOutputStream(response, sampleContent);
    }
}
