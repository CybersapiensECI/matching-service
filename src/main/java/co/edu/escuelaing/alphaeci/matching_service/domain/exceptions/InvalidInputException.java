package co.edu.escuelaing.alphaeci.matching_service.domain.exceptions;
import org.springframework.http.HttpStatus;


public class InvalidInputException extends MatchProfileException {

    public InvalidInputException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

}
