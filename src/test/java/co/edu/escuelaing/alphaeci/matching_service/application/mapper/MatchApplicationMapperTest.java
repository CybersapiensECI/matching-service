package co.edu.escuelaing.alphaeci.matching_service.application.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import co.edu.escuelaing.alphaeci.matching_service.application.dto.request.AffinityScoreRequest;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.request.AffinityScoreUpdateRequest;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.request.MatchRequest;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.request.MatchUpdateRequest;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.response.AffinityScoreResponse;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.response.MatchResponse;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.response.RecommendationResponse;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.Match;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.enums.MatchStatus;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.AffinityScore;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MatchApplicationMapperTest {

    private MatchApplicationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(MatchApplicationMapper.class);
    }

    // ======================== toDomain(MatchRequest) ========================

    @Test
    void toDomain_matchRequest_mapsRequesterAndTarget() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        MatchRequest request = new MatchRequest();
        request.setRequesterId(requesterId);
        request.setTargetId(targetId);

        Match result = mapper.toDomain(request);

        assertThat(result).isNotNull();
        assertThat(result.getRequesterId()).isEqualTo(requesterId);
        assertThat(result.getTargetId()).isEqualTo(targetId);
        assertThat(result.getIdMatch()).isNull();
        assertThat(result.getStatus()).isNull();
        assertThat(result.getAffinityScore()).isNull();
    }

    @Test
    void toDomain_matchRequest_null_returnsNull() {
        assertThat(mapper.toDomain((MatchRequest) null)).isNull();
    }

    // ======================== toDomain(AffinityScoreRequest) ========================

    @Test
    void toDomain_affinityScoreRequest_mapsTotalScore() {
        AffinityScoreRequest request = new AffinityScoreRequest();
        request.setScore(0.85);
        request.setInterestScore(0.9);
        request.setScheduleScore(0.7);

        AffinityScore result = mapper.toDomain(request);

        assertThat(result).isNotNull();
        assertThat(result.getTotalScore()).isEqualTo(0.85);
        assertThat(result.getInterestScore()).isEqualTo(0.9);
        assertThat(result.getScheduleScore()).isEqualTo(0.7);
        assertThat(result.getAcademicScore()).isEqualTo(0.0); // ignored
    }

    @Test
    void toDomain_affinityScoreRequest_null_returnsNull() {
        assertThat(mapper.toDomain((AffinityScoreRequest) null)).isNull();
    }

    // ======================== updateAffinityScoreFromRequest ========================

    @Test
    void updateAffinityScoreFromRequest_updatesNonNullFields() {
        AffinityScoreUpdateRequest request = new AffinityScoreUpdateRequest();
        request.setScore(0.75);
        request.setInterestScore(0.8);

        AffinityScore existing = new AffinityScore();
        existing.setTotalScore(0.5);
        existing.setScheduleScore(0.6);

        mapper.updateAffinityScoreFromRequest(request, existing);

        assertThat(existing.getTotalScore()).isEqualTo(0.75);
        assertThat(existing.getInterestScore()).isEqualTo(0.8);
        assertThat(existing.getScheduleScore()).isEqualTo(0.6); // unchanged
    }

    // ======================== toResponse(Match) ========================

    @Test
    void toResponse_match_mapsAllFields() {
        UUID matchId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        AffinityScore score = new AffinityScore();
        score.setTotalScore(0.9);
        score.setInterestScore(0.85);
        score.setScheduleScore(0.75);

        Match match = new Match();
        match.setIdMatch(matchId);
        match.setRequesterId(requesterId);
        match.setTargetId(targetId);
        match.setStatus(MatchStatus.ACCEPTED);
        match.setAffinityScore(score);
        match.setCreatedAt(now);
        match.setUpdatedAt(now);

        MatchResponse response = mapper.toResponse(match);

        assertThat(response).isNotNull();
        assertThat(response.getIdMatch()).isEqualTo(matchId);
        assertThat(response.getRequesterId()).isEqualTo(requesterId);
        assertThat(response.getTargetId()).isEqualTo(targetId);
        assertThat(response.getStatus()).isEqualTo(MatchStatus.ACCEPTED);
        assertThat(response.getAffinityScore()).isNotNull();
        assertThat(response.getAffinityScore().getScore()).isEqualTo(0.9);
    }

    @Test
    void toResponse_match_null_returnsNull() {
        assertThat(mapper.toResponse((Match) null)).isNull();
    }

    // ======================== toResponse(AffinityScore) ========================

    @Test
    void toResponse_affinityScore_mapsTotalToScore() {
        AffinityScore score = new AffinityScore();
        score.setTotalScore(0.88);
        score.setInterestScore(0.9);
        score.setScheduleScore(0.7);
        score.setAcademicScore(0.85);

        AffinityScoreResponse response = mapper.toResponse(score);

        assertThat(response).isNotNull();
        assertThat(response.getScore()).isEqualTo(0.88);
        assertThat(response.getInterestScore()).isEqualTo(0.9);
        assertThat(response.getScheduleScore()).isEqualTo(0.7);
        assertThat(response.getAcademicScore()).isEqualTo(0.0); // ignored
    }

    @Test
    void toResponse_affinityScore_null_returnsNull() {
        assertThat(mapper.toResponse((AffinityScore) null)).isNull();
    }

    // ======================== toResponseList ========================

    @Test
    void toResponseList_mapsList() {
        Match m1 = new Match();
        m1.setIdMatch(UUID.randomUUID());
        m1.setStatus(MatchStatus.PENDING);

        Match m2 = new Match();
        m2.setIdMatch(UUID.randomUUID());
        m2.setStatus(MatchStatus.REJECTED);

        List<MatchResponse> result = mapper.toResponseList(List.of(m1, m2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStatus()).isEqualTo(MatchStatus.PENDING);
        assertThat(result.get(1).getStatus()).isEqualTo(MatchStatus.REJECTED);
    }

    @Test
    void toResponseList_null_returnsNull() {
        assertThat(mapper.toResponseList(null)).isNull();
    }

    // ======================== toRecommendationResponse ========================

    @Test
    void toRecommendationResponse_mapsCorrectly() {
        UUID userId = UUID.randomUUID();
        List<UUID> recommendedIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        RecommendationResponse response = mapper.toRecommendationResponse(userId, recommendedIds);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getRecommendedUserIds()).isEqualTo(recommendedIds);
    }

    // ======================== updateMatchFromRequest ========================

    @Test
    void updateMatchFromRequest_updatesStatus() {
        MatchUpdateRequest request = new MatchUpdateRequest();
        request.setStatus(MatchStatus.ACCEPTED);

        Match match = new Match();
        match.setIdMatch(UUID.randomUUID());
        match.setStatus(MatchStatus.PENDING);

        mapper.updateMatchFromRequest(request, match);

        assertThat(match.getStatus()).isEqualTo(MatchStatus.ACCEPTED);
        assertThat(match.getIdMatch()).isNotNull(); // preserved
    }
}
