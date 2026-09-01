package com.syslog.api.controller;

import com.syslog.api.model.dtos.AuthRequestDTO;
import com.syslog.api.model.dtos.AuthResponseDTO;
import com.syslog.api.model.dtos.ChangePasswordRequestDTO;
import com.syslog.api.service.CredentialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/credential", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Tag(name = "Credential", description = "Credential API")
public class CredentialController {

    private final CredentialService credentialService;

    @PostMapping
    @Operation(summary = "Log in using the email address provided.",
            description = "Log in using the email address provided.", tags = {
            "Credential"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logged in")})
    public ResponseEntity<AuthResponseDTO<Void>> login(@Valid @RequestBody AuthRequestDTO authRequestDTO) {
        credentialService.authenticate(authRequestDTO.getEmail().toLowerCase(), authRequestDTO.getPassword());
        return ResponseEntity.ok().build();
    }

    @PutMapping
    @Operation(summary = "Change password using the email address provided.",
            description = "Change password using the email address provided.", tags = {
            "Credential"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed")})
    public ResponseEntity<AuthResponseDTO<Void>> changePassword(@Valid @RequestBody ChangePasswordRequestDTO request) {
        credentialService.changePassword(request.getEmail().toLowerCase(), request);
        return ResponseEntity.ok().build();
    }
}
