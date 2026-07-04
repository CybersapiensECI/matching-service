package co.edu.escuelaing.alphaeci.matching_service.domain.exceptions;

import org.springframework.http.HttpStatus;

public class NotFoundException extends MatchProfileException {
    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
