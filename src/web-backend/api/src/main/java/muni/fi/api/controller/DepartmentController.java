package muni.fi.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import muni.fi.bl.service.DepartmentService;
import muni.fi.dtos.DepartmentDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/departments")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @Operation(summary = "Retrieve all project departments")
    @GetMapping
    public List<DepartmentDto> getAll() {
        log.info("Retrieve all project departments");
        return departmentService.getAll();
    }

}
