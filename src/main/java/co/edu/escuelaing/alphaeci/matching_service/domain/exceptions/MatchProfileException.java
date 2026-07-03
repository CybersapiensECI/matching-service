package co.edu.escuelaing.alphaeci.matching_service.domain.exceptions;

import org.springframework.http.HttpStatus;

public class MatchProfileException extends RuntimeException {
    private final HttpStatus status;
    public MatchProfileException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
