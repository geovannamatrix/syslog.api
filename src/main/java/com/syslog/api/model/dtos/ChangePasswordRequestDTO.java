package com.syslog.api.model.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class ChangePasswordRequestDTO {

    @NotNull
    private String email;
    @NotNull
    private String password;
    @NotNull
    private String newPassword;
}
