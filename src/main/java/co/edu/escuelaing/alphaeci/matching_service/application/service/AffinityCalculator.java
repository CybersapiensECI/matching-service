package co.edu.escuelaing.alphaeci.matching_service.application.service;

import co.edu.escuelaing.alphaeci.matching_service.domain.model.MatchProfile;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.AffinityScore;

public interface AffinityCalculator {
    AffinityScore calculate(MatchProfile requester, MatchProfile target);
}
