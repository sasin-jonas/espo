package muni.fi.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import muni.fi.bl.service.UserService;
import muni.fi.dtos.UserDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get all users")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserDto> getAll() {
        log.info("Getting all users");
        return userService.getAllUsers();
    }

    @Operation(summary = "Get current user")
    @GetMapping("/me")
    public UserDto getMe() {
        String securityId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Get representation for user {}", securityId);
        return userService.getUser(securityId);
    }

    @Operation(summary = "Get admin emails")
    @GetMapping("/admins")
    public List<String> getAdminEmails() {
        log.info("Retrieving admin users");
        return userService.getAdmins()
                .stream()
                .map(UserDto::getEmail)
                .toList();
    }

    @Operation(summary = "Update a user by ID")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void updateUser(
            @Parameter(description = "The ID of the user to update") @PathVariable Long id,
            @Parameter(description = "The updated information for the user") @RequestBody UserDto userDto) {
        log.info("Updating user with id {} and with updated properties: {}", id, userDto);
        userService.updateUser(id, userDto);
    }
}
