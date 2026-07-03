package co.edu.escuelaing.alphaeci.matching_service.domain.exceptions;

import org.springframework.http.HttpStatus;

public class ExternalServiceException extends MatchProfile {
    public ExternalServiceException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
