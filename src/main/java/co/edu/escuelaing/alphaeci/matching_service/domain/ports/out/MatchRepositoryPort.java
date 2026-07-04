package co.edu.escuelaing.alphaeci.matching_service.domain.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.escuelaing.alphaeci.matching_service.domain.model.Match;

public interface MatchRepositoryPort {
    Match save(Match match);
    Optional<Match> findById(UUID id);
    List<Match> findByTargetId(UUID targetId);
    List<Match> findByRequesterId(UUID requesterId);
    boolean existsByRequesterIdAndTargetId(UUID requesterId, UUID targetId);
    void delete(UUID matchId);
}
