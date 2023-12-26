package ru.nsu.digitallibrary.config;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.nsu.digitallibrary.exception.ClientException;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler({ClientException.class})
    protected ResponseEntity<ApiErrorResponse> handleApiException(ClientException ex) {
        return new ResponseEntity<>(new ApiErrorResponse(ex.getCode(), ex.getMessage()), ex.getCode());
    }

    @Data
    public class ApiErrorResponse {

        private final HttpStatus status;
        private final String message;
    }
}
