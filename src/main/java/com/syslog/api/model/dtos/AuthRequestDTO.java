package com.syslog.api.model.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "DTO for authentication request")
public class AuthRequestDTO {

    @NotNull
    @Schema(description = "User email for authentication")
    private String email;

    @NotNull
    @Schema(description = "User password for authentication")
    private String password;
}