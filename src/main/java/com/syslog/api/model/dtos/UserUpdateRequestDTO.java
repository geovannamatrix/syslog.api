package com.syslog.api.model.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO for user update request")
public class UserUpdateRequestDTO {

    @Schema(description = "User email for update")
    @Email
    private String email;

    @Schema(description = "Username for update")
    private String username;

    @Schema(description = "User password for update")
    @Size(min = 8, max = 72)
    private String password;
}
