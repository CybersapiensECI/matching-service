package co.edu.escuelaing.alphaeci.matching_service.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.escuelaing.alphaeci.matching_service.application.service.AffinityCalculator;
import co.edu.escuelaing.alphaeci.matching_service.domain.exceptions.NoRecommendationsFoundException;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.MatchProfile;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.NearbyRecommendation;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.NearbyUserDistance;
import co.edu.escuelaing.alphaeci.matching_service.domain.ports.out.GeolocationServicePort;
import co.edu.escuelaing.alphaeci.matching_service.domain.ports.out.ProfileServicePort;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.AffinityScore;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationsUseCaseImplTest {

    @Mock
    private ProfileServicePort profileServicePort;

    @Mock
    private GeolocationServicePort geolocationServicePort;

    @Mock
    private AffinityCalculator affinityCalculator;

    @InjectMocks
    private RecommendationsUseCaseImpl recommendationsUseCase;

    private UUID userId;
    private UUID otherId1;
    private UUID otherId2;
    private MatchProfile userProfile;
    private MatchProfile otherProfile1;
    private MatchProfile otherProfile2;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        otherId1 = UUID.randomUUID();
        otherId2 = UUID.randomUUID();

        userProfile = new MatchProfile();
        userProfile.setId(userId);

        otherProfile1 = new MatchProfile();
        otherProfile1.setId(otherId1);

        otherProfile2 = new MatchProfile();
        otherProfile2.setId(otherId2);
    }

    private AffinityScore scoreWith(double total) {
        AffinityScore s = new AffinityScore();
        s.setTotalScore(total);
        return s;
    }

    @Test
    void getRecommendationsForUser_excludesSelf() {
        when(profileServicePort.getProfileById(userId)).thenReturn(userProfile);
        // getAllProfiles(userId) already excludes the requester
        when(profileServicePort.getAllProfiles(userId)).thenReturn(List.of(otherProfile1));
        when(affinityCalculator.calculate(any(), any())).thenReturn(scoreWith(0.8));

        Map<UUID, AffinityScore> result = recommendationsUseCase.getRecommendationsForUser(userId);

        assertThat(result).doesNotContainKey(userId);
        assertThat(result).containsKey(otherId1);
    }

    @Test
    void getRecommendationsForUser_returnsMapWithAffinityScores() {
        when(profileServicePort.getProfileById(userId)).thenReturn(userProfile);
        when(profileServicePort.getAllProfiles(userId)).thenReturn(List.of(otherProfile1, otherProfile2));
        AffinityScore score1 = scoreWith(0.7);
        AffinityScore score2 = scoreWith(0.5);
        when(affinityCalculator.calculate(userProfile, otherProfile1)).thenReturn(score1);
        when(affinityCalculator.calculate(userProfile, otherProfile2)).thenReturn(score2);

        Map<UUID, AffinityScore> result = recommendationsUseCase.getRecommendationsForUser(userId);

        assertThat(result).hasSize(2);
        assertThat(result.get(otherId1).getTotalScore()).isEqualTo(0.7);
        assertThat(result.get(otherId2).getTotalScore()).isEqualTo(0.5);
    }

    @Test
    void getRecommendedProfilesForUser_sortedByAffinityDescending() {
        when(profileServicePort.getProfileById(userId)).thenReturn(userProfile);
        // called twice: once by getRecommendationsForUser, once by getAllOtherProfiles inside getRecommendedProfilesForUser
        when(profileServicePort.getAllProfiles(userId)).thenReturn(List.of(otherProfile1, otherProfile2));
        when(affinityCalculator.calculate(userProfile, otherProfile1)).thenReturn(scoreWith(0.3));
        when(affinityCalculator.calculate(userProfile, otherProfile2)).thenReturn(scoreWith(0.9));

        List<MatchProfile> result = recommendationsUseCase.getRecommendedProfilesForUser(userId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(otherId2); // highest affinity first
        assertThat(result.get(1).getId()).isEqualTo(otherId1);
    }

    @Test
    void getRecommendedUserIdsForUser_returnsOrderedIds() {
        when(profileServicePort.getProfileById(userId)).thenReturn(userProfile);
        when(profileServicePort.getAllProfiles(userId)).thenReturn(List.of(otherProfile1, otherProfile2));
        when(affinityCalculator.calculate(userProfile, otherProfile1)).thenReturn(scoreWith(0.2));
        when(affinityCalculator.calculate(userProfile, otherProfile2)).thenReturn(scoreWith(0.8));

        List<UUID> result = recommendationsUseCase.getRecommendedUserIdsForUser(userId);

        assertThat(result).containsExactly(otherId2, otherId1);
    }

    @Test
    void calculateAffinityScore_callsCalculator() {
        UUID userId2 = UUID.randomUUID();
        MatchProfile profile2 = new MatchProfile();
        profile2.setId(userId2);

        when(profileServicePort.getProfileById(userId)).thenReturn(userProfile);
        when(profileServicePort.getProfileById(userId2)).thenReturn(profile2);
        AffinityScore expected = scoreWith(0.75);
        when(affinityCalculator.calculate(userProfile, profile2)).thenReturn(expected);

        AffinityScore result = recommendationsUseCase.calculateAffinityScore(userId, userId2);

        assertThat(result.getTotalScore()).isEqualTo(0.75);
    }

    @Test
    void getRecommendationsForUser_onlyUser_throwsNoRecommendationsFoundException() {
        when(profileServicePort.getProfileById(userId)).thenReturn(userProfile);
        // profile service excludes the requester, so returns empty list
        when(profileServicePort.getAllProfiles(userId)).thenReturn(List.of());

        assertThatThrownBy(() -> recommendationsUseCase.getRecommendationsForUser(userId))
                .isInstanceOf(NoRecommendationsFoundException.class)
                .hasMessageContaining("couldn't find anyone");
    }

    @Test
    void getRecommendationsForUser_returnsAllOtherProfiles() {
        when(profileServicePort.getProfileById(userId)).thenReturn(userProfile);
        when(profileServicePort.getAllProfiles(userId)).thenReturn(List.of(otherProfile1, otherProfile2));
        when(affinityCalculator.calculate(userProfile, otherProfile1)).thenReturn(scoreWith(0.7));
        when(affinityCalculator.calculate(userProfile, otherProfile2)).thenReturn(scoreWith(0.5));

        Map<UUID, AffinityScore> result = recommendationsUseCase.getRecommendationsForUser(userId);

        assertThat(result).containsKeys(otherId1, otherId2).doesNotContainKey(userId);
    }

    @Test
    void getNearbyRecommendationsForUser_returnsNearbyProfilesWithDistance() {
        when(profileServicePort.getProfileById(userId)).thenReturn(userProfile);
        when(profileServicePort.getAllProfiles(userId)).thenReturn(List.of(otherProfile1, otherProfile2));
        when(geolocationServicePort.getNearbyUsers(userId)).thenReturn(List.of(
                new NearbyUserDistance(otherId1, 120.0),
                new NearbyUserDistance(otherId2, 45.0)
        ));
        when(affinityCalculator.calculate(userProfile, otherProfile1)).thenReturn(scoreWith(0.4));
        when(affinityCalculator.calculate(userProfile, otherProfile2)).thenReturn(scoreWith(0.9));

        List<NearbyRecommendation> result = recommendationsUseCase.getNearbyRecommendationsForUser(userId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUserId()).isEqualTo(otherId2);
        assertThat(result.get(0).getDistanceMeters()).isEqualTo(45.0);
        assertThat(result.get(1).getUserId()).isEqualTo(otherId1);
        assertThat(result.get(1).getDistanceMeters()).isEqualTo(120.0);
    }
}
