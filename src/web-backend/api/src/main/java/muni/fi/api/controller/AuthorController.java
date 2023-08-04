package muni.fi.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import muni.fi.bl.service.AuthorService;
import muni.fi.dtos.AuthorDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/authors")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @Operation(summary = "Retrieve all project authors")
    @GetMapping
    public List<AuthorDto> getAll() {
        log.info("Retrieve all project authors");
        return authorService.getAll();
    }

}
