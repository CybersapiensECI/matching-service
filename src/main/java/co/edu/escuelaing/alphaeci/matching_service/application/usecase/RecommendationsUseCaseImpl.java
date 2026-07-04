package co.edu.escuelaing.alphaeci.matching_service.application.usecase;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import co.edu.escuelaing.alphaeci.matching_service.application.dto.request.FilterCriteriaRequest;
import co.edu.escuelaing.alphaeci.matching_service.application.service.AffinityCalculator;
import co.edu.escuelaing.alphaeci.matching_service.domain.exceptions.NoRecommendationsFoundException;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.MatchProfile;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.NearbyRecommendation;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.NearbyUserDistance;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.enums.CareersEnum;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.enums.SemesterEnum;
import co.edu.escuelaing.alphaeci.matching_service.domain.ports.in.RecommendationsUseCasePort;
import co.edu.escuelaing.alphaeci.matching_service.domain.ports.out.GeolocationServicePort;
import co.edu.escuelaing.alphaeci.matching_service.domain.ports.out.ProfileServicePort;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.AffinityScore;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationsUseCaseImpl implements RecommendationsUseCasePort {

    private final ProfileServicePort profileServicePort;
    private final GeolocationServicePort geolocationServicePort;
    private final AffinityCalculator affinityCalculator;

    @Override
    public Map<UUID, AffinityScore> getRecommendationsForUser(UUID userId) {
        MatchProfile requester = profileServicePort.getProfileById(userId);
        List<MatchProfile> otherProfiles = getAllOtherProfiles(userId);

        Map<UUID, AffinityScore> recommendations = new HashMap<>();
        for (MatchProfile target : otherProfiles) {
            recommendations.put(target.getId(), affinityCalculator.calculate(requester, target));
        }

        Map<UUID, AffinityScore> result = recommendations.entrySet().stream()
                .sorted(Map.Entry.<UUID, AffinityScore>comparingByValue(
                        Comparator.comparingDouble(AffinityScore::getTotalScore)).reversed())
                .limit(20)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));

        if (result.isEmpty()) {
            throw new NoRecommendationsFoundException();
        }
        return result;
    }

    @Override
    public List<MatchProfile> getRecommendedProfilesForUser(UUID userId) {
        Map<UUID, AffinityScore> scores = getRecommendationsForUser(userId);
        return getAllOtherProfiles(userId).stream()
                .filter(profile -> scores.containsKey(profile.getId()))
                .sorted(Comparator
                        .comparingDouble((MatchProfile profile) -> scores.get(profile.getId()).getTotalScore())
                        .reversed())
                .limit(20)
                .toList();
    }

    @Override
    public List<UUID> getRecommendedUserIdsForUser(UUID userId) {
        return getRecommendationsForUser(userId).entrySet().stream()
                .sorted(Map.Entry.<UUID, AffinityScore>comparingByValue(
                        Comparator.comparingDouble(AffinityScore::getTotalScore)).reversed())
                .limit(20)
                .map(Map.Entry::getKey)
                .toList();
    }

    @Override
    public List<NearbyRecommendation> getNearbyRecommendationsForUser(UUID userId) {
        MatchProfile requester = profileServicePort.getProfileById(userId);
        List<NearbyUserDistance> nearbyUsers = geolocationServicePort.getNearbyUsers(userId);
        Map<UUID, Double> distanceByUserId = nearbyUsers.stream()
                .collect(Collectors.toMap(NearbyUserDistance::getUserId, NearbyUserDistance::getDistanceMeters));
        Set<UUID> nearbyUserIds = new HashSet<>(distanceByUserId.keySet());

        List<NearbyRecommendation> result = getAllOtherProfiles(userId).stream()
                .filter(profile -> nearbyUserIds.contains(profile.getId()))
                .map(profile -> new NearbyRecommendation(
                        new NearbyUserDistance(
                                profile.getId(),
                                distanceByUserId.get(profile.getId())),
                        affinityCalculator.calculate(requester, profile)))
                .sorted(Comparator.comparingDouble(
                        (NearbyRecommendation recommendation) -> recommendation.getAffinityScore().getTotalScore())
                        .reversed())
                .limit(20)
                .toList();

        if (result.isEmpty()) {
            throw new NoRecommendationsFoundException();
        }
        return result;
    }

    @Override
    public AffinityScore calculateAffinityScore(UUID userId1, UUID userId2) {
        MatchProfile a = profileServicePort.getProfileById(userId1);
        MatchProfile b = profileServicePort.getProfileById(userId2);
        return affinityCalculator.calculate(a, b);
    }

    private List<MatchProfile> getAllOtherProfiles(UUID userId) {
        return profileServicePort.getAllProfiles(userId);
    }

    @Override
    public List<UUID> getFilteredRecommendations(UUID userId, FilterCriteriaRequest filters) {
        List<MatchProfile> candidates;
        if (filters.isGeolocation()) {
            Set<UUID> nearbyIds = getNearbyRecommendationsForUser(userId).stream()
                    .map(NearbyRecommendation::getUserId)
                    .collect(Collectors.toSet());
            candidates = getAllOtherProfiles(userId).stream()
                    .filter(p -> nearbyIds.contains(p.getId()))
                    .toList();
        } else {
            candidates = getRecommendedProfilesForUser(userId);
        }

        List<UUID> result = candidates.stream()
                .filter(p -> !filters.isActive() || p.isActive())
                .filter(p -> filters.getCareers() == null || filters.getCareers() == CareersEnum.ALL
                        || filters.getCareers().name().equalsIgnoreCase(p.getCareer()))
                .filter(p -> filters.getSemesters() == null || filters.getSemesters() == SemesterEnum.ALL
                        || filters.getSemesters().ordinal() + 1 == p.getSemester())
                .filter(p -> filters.getTag() == null
                        || (p.getTags() != null && p.getTags().contains(filters.getTag())))
                .map(MatchProfile::getId)
                .limit(20)
                .toList();

        if (result.isEmpty()) {
            throw new NoRecommendationsFoundException();
        }
        return result;
    }
}
