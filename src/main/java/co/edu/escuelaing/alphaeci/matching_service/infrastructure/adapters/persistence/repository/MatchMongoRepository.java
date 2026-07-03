package co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.entity.MatchDocument;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchMongoRepository extends MongoRepository<MatchDocument, UUID> {
    List<MatchDocument> findByRequesterId(UUID requesterId);

    List<MatchDocument> findByTargetId(UUID targetId);

    boolean existsByRequesterIdAndTargetId(UUID requesterId, UUID targetId);
}
