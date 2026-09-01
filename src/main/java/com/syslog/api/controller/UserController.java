package com.syslog.api.controller;

import com.syslog.api.model.dtos.AuthResponseDTO;
import com.syslog.api.model.dtos.UserRequestDTO;
import com.syslog.api.model.dtos.UserResponseDTO;
import com.syslog.api.model.dtos.UserUpdateRequestDTO;
import com.syslog.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create a new user", description = "Create a new user", tags = {
            "User"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created the new user")})
    public ResponseEntity<AuthResponseDTO<UserResponseDTO>> create(@Valid @RequestBody UserRequestDTO request) {
        var userId = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponseDTO<>(userId));
    }

    @PatchMapping("/{email}")
    @Operation(summary = "Update the user by email", description = "Update the user by email", tags = {
            "User"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated the user")})
    public ResponseEntity<AuthResponseDTO<Void>> update(@PathVariable String email, @Valid @RequestBody UserUpdateRequestDTO request) {
        userService.updateUser(email, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete the user by id", description = "Delete the user by id", tags = {
            "User"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted the user")})
    public ResponseEntity<AuthResponseDTO<Void>> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
