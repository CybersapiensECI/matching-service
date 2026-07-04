package co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.mapper;


import org.mapstruct.Mapper;

import co.edu.escuelaing.alphaeci.matching_service.domain.model.Match;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.AffinityScore;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.entity.AffinityScoreDocument;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.entity.MatchDocument;

@Mapper(componentModel = "spring")
public interface MatchPersistenceMapper {
    
    MatchDocument toDocument(Match match);

    Match toDomain(MatchDocument document);

    AffinityScoreDocument toAffinityDocument(AffinityScore affinityScore);

    AffinityScore toAffinityDomain(AffinityScoreDocument document);
}
