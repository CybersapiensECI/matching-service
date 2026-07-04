package co.edu.escuelaing.alphaeci.matching_service.domain.ports.in;

import java.util.UUID;

import co.edu.escuelaing.alphaeci.matching_service.domain.model.Match;

import java.util.List;


public interface MatchUseCasePort {

    Match createMatch(UUID requesterId, UUID targetId);
    Match getMatch(UUID matchId);
    List<Match> findByRequesterId(UUID requesterId);
    List<Match> findByTargetId(UUID targetId);
    Match respondToMatchRequest(UUID matchId, UUID responderId, boolean accept);
    void cancelMatch(UUID matchId, UUID requesterId);

}
