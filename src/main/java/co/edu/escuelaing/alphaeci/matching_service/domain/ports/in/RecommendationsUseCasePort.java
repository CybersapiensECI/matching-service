package co.edu.escuelaing.alphaeci.matching_service.domain.ports.in;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import co.edu.escuelaing.alphaeci.matching_service.application.dto.request.FilterCriteriaRequest;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.MatchProfile;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.NearbyRecommendation;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.AffinityScore;


public interface RecommendationsUseCasePort {

    Map<UUID, AffinityScore> getRecommendationsForUser(UUID userId);
    List<MatchProfile> getRecommendedProfilesForUser(UUID userId);
    List<UUID> getRecommendedUserIdsForUser(UUID userId);
    List<NearbyRecommendation> getNearbyRecommendationsForUser(UUID userId);
    AffinityScore calculateAffinityScore(UUID userId1, UUID userId2);
    List<UUID> getFilteredRecommendations(UUID userId, FilterCriteriaRequest filters);
}
