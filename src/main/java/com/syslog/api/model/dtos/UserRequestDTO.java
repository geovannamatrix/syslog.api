package com.syslog.api.model.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Schema(description = "DTO for user request")
public class UserRequestDTO {

    @Schema(description = "User name for registration")
    @NotBlank
    private String name;

    @Schema(description = "User email for registration")
    @NotBlank
    @Email
    private String email;

    @Schema(description = "Username for registration")
    @NotBlank
    private String username;

    @Schema(description = "User password for registration")
    @NotBlank
    @Size(min = 8, max = 72)
    private String password;
}
