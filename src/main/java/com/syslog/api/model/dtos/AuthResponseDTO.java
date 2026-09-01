package com.syslog.api.model.dtos;

import com.syslog.api.filter.ControllerAdvisor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "DTO for authentication response")
public class AuthResponseDTO<T> {

    @Schema(description = "Response message")
    private T response;
    @Schema(description = "List of errors")
    private List<ControllerAdvisor.ErrorDTO> errors;

    public AuthResponseDTO(T message) {
        this.response = message;
    }
}
