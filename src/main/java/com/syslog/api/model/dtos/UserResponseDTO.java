package com.syslog.api.model.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "User Response DTO")
public class UserResponseDTO {

    private Long userId;
}
