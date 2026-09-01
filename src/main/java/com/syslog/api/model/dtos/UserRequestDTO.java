package com.syslog.api.model.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO for user request")
public class UserRequestDTO {

    @Schema(description = "User email for registration")
    private String email;

    @Schema(description = "Username for registration")
    private String username;

    @Schema(description = "User password for registration")
    private String password;
}
