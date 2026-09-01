package com.syslog.api.exception;

public class ExternalServerErrorException extends RuntimeException {

    public ExternalServerErrorException(String message) {
        super(message);
    }

    public ExternalServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }

}
