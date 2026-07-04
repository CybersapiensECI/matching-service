package co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.mapper;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import co.edu.escuelaing.alphaeci.matching_service.domain.model.Match;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.enums.MatchStatus;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.AffinityScore;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.entity.AffinityScoreDocument;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.entity.MatchDocument;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MatchPersistenceMapperTest {

    private MatchPersistenceMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(MatchPersistenceMapper.class);
    }

    // ======================== toDocument(Match) ========================

    @Test
    void toDocument_mapsAllFields() {
        UUID matchId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        AffinityScore score = new AffinityScore();
        score.setTotalScore(0.9);
        score.setInterestScore(0.85);
        score.setAcademicScore(0.7);
        score.setScheduleScore(0.8);

        Match match = new Match();
        match.setIdMatch(matchId);
        match.setRequesterId(requesterId);
        match.setTargetId(targetId);
        match.setStatus(MatchStatus.ACCEPTED);
        match.setAffinityScore(score);
        match.setCreatedAt(now);
        match.setUpdatedAt(now);

        MatchDocument doc = mapper.toDocument(match);

        assertThat(doc).isNotNull();
        assertThat(doc.getIdMatch()).isEqualTo(matchId);
        assertThat(doc.getRequesterId()).isEqualTo(requesterId);
        assertThat(doc.getTargetId()).isEqualTo(targetId);
        assertThat(doc.getStatus()).isEqualTo(MatchStatus.ACCEPTED);
        assertThat(doc.getCreatedAt()).isEqualTo(now);
        assertThat(doc.getAffinityScore()).isNotNull();
        assertThat(doc.getAffinityScore().getInterestScore()).isEqualTo(0.85);
    }

    @Test
    void toDocument_null_returnsNull() {
        assertThat(mapper.toDocument((Match) null)).isNull();
    }

    // ======================== toDomain(MatchDocument) ========================

    @Test
    void toDomain_matchDocument_mapsAllFields() {
        UUID matchId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        AffinityScoreDocument scoreDoc = new AffinityScoreDocument(0.0, 0.9, 0.7, 0.8);

        MatchDocument doc = new MatchDocument(matchId, requesterId, targetId,
                MatchStatus.PENDING, scoreDoc, now, now);

        Match result = mapper.toDomain(doc);

        assertThat(result).isNotNull();
        assertThat(result.getIdMatch()).isEqualTo(matchId);
        assertThat(result.getRequesterId()).isEqualTo(requesterId);
        assertThat(result.getTargetId()).isEqualTo(targetId);
        assertThat(result.getStatus()).isEqualTo(MatchStatus.PENDING);
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getAffinityScore()).isNotNull();
        assertThat(result.getAffinityScore().getInterestScore()).isEqualTo(0.9);
    }

    @Test
    void toDomain_matchDocument_null_returnsNull() {
        assertThat(mapper.toDomain((MatchDocument) null)).isNull();
    }

    // ======================== toAffinityDocument ========================

    @Test
    void toAffinityDocument_mapsCorrectly() {
        AffinityScore score = new AffinityScore();
        score.setTotalScore(0.88);
        score.setInterestScore(0.9);
        score.setAcademicScore(0.75);
        score.setScheduleScore(0.7);

        AffinityScoreDocument doc = mapper.toAffinityDocument(score);

        assertThat(doc).isNotNull();
        assertThat(doc.getInterestScore()).isEqualTo(0.9);
        assertThat(doc.getAcademicScore()).isEqualTo(0.75);
        assertThat(doc.getScheduleScore()).isEqualTo(0.7);
        // totalScore is mapped to "score" field -> not set (ignored in AffinityScoreDocument)
    }

    @Test
    void toAffinityDocument_null_returnsNull() {
        assertThat(mapper.toAffinityDocument(null)).isNull();
    }

    // ======================== toAffinityDomain ========================

    @Test
    void toAffinityDomain_mapsCorrectly() {
        AffinityScoreDocument doc = new AffinityScoreDocument(0.0, 0.85, 0.6, 0.75);

        AffinityScore result = mapper.toAffinityDomain(doc);

        assertThat(result).isNotNull();
        assertThat(result.getInterestScore()).isEqualTo(0.85);
        assertThat(result.getAcademicScore()).isEqualTo(0.6);
        assertThat(result.getScheduleScore()).isEqualTo(0.75);
    }

    @Test
    void toAffinityDomain_null_returnsNull() {
        assertThat(mapper.toAffinityDomain(null)).isNull();
    }

    // ======================== toDocument with null affinityScore ========================

    @Test
    void toDocument_withNullAffinityScore_setsNullInDocument() {
        Match match = new Match();
        match.setIdMatch(UUID.randomUUID());
        match.setAffinityScore(null);

        MatchDocument doc = mapper.toDocument(match);

        assertThat(doc).isNotNull();
        assertThat(doc.getAffinityScore()).isNull();
    }
}
