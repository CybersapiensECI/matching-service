package co.edu.escuelaing.alphaeci.matching_service.domain.exceptions;

import org.springframework.http.HttpStatus;

public class NoRecommendationsFoundException extends MatchProfile {
    public NoRecommendationsFoundException() {
        super("We couldn't find anyone who matches you. Try again later!", HttpStatus.NOT_FOUND);
    }
}
