package co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.adapter;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import co.edu.escuelaing.alphaeci.matching_service.domain.model.Match;
import co.edu.escuelaing.alphaeci.matching_service.domain.ports.out.MatchRepositoryPort;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.mapper.MatchPersistenceMapper;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.repository.MatchMongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MatchRepositoryAdapter implements MatchRepositoryPort {

    private final MatchMongoRepository mongoRepository;
    private final MatchPersistenceMapper mapper;

    @Override
    public Match save(Match match) {
        return mapper.toDomain(mongoRepository.save(mapper.toDocument(match)));
    }

    @Override
    public Optional<Match> findById(UUID id) {
        return mongoRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByRequesterIdAndTargetId(UUID requesterId, UUID targetId) {
        return mongoRepository.existsByRequesterIdAndTargetId(requesterId, targetId);
    }

    @Override
    public List<Match> findByTargetId(UUID targetId) {
        return mongoRepository.findByTargetId(targetId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Match> findByRequesterId(UUID requesterId) {
        return mongoRepository.findByRequesterId(requesterId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void delete(UUID matchId) {
        mongoRepository.deleteById(matchId);
    }
}
