package org.emporio.sabor.real.api.filter;

import lombok.extern.slf4j.Slf4j;
import org.emporio.sabor.real.api.exception.BadRequestException;
import org.emporio.sabor.real.api.exception.ExternalServerErrorException;
import org.emporio.sabor.real.api.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;

@ControllerAdvice
@Slf4j
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ControllerAdvisor {

    public record ErrorDTO(String code, String message) {
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity handleNotFoundErrors(Exception ex) {
        log.error("Not Found exception: {}", ex.getMessage(), ex);
        var errorMsg = ex.getMessage();
        var errorCode = HttpStatus.NOT_FOUND.toString();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDTO(errorCode, errorMsg));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity handleBadRequestErrors(Exception ex) {
        log.error("Bad Request exception: {}", ex.getMessage(), ex);
        var errorMsg = ex.getMessage();
        var errorCode = HttpStatus.BAD_REQUEST.toString();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
              new ErrorDTO(errorCode, errorMsg));
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity handleInternalError(Exception ex) {
        log.error("Internal Error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorDTO(
              HttpStatus.INTERNAL_SERVER_ERROR.toString(), ex.getMessage()));
    }

    @ExceptionHandler(ExternalServerErrorException.class)
    public ResponseEntity handleExternalServerError(Exception ex) {
        log.error("External Server Error: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body( new ErrorDTO(
              HttpStatus.INTERNAL_SERVER_ERROR.toString(), ex.getMessage()));
    }
}