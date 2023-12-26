package ru.nsu.digitallibrary.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
public class ClientException extends RuntimeException {
    private final HttpStatus code;
    private final String message;

    private ClientException(HttpStatus code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ClientException of(HttpStatus code, String message) {
        return new ClientException(code, message);
    }

    public ResponseEntity<?> response() {
        return ResponseEntity.status(code.value()).body(new Response(code, message));
    }

    @Data
    @AllArgsConstructor
    static public class Response {
        private HttpStatus status;
        private String message;
    }
}
