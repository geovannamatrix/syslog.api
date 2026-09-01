package com.syslog.api.model.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "User credentials data transfer object")
public class CredentialDTO {

    @Schema(description =  "User id")
    private Long id;

    @Schema(description =  "User email")
    private String email;

    @Schema(description =  "User password")
    private String password;
}
