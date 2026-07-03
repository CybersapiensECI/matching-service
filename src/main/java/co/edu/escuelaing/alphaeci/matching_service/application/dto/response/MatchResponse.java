package co.edu.escuelaing.alphaeci.matching_service.application.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

import co.edu.escuelaing.alphaeci.matching_service.domain.model.enums.MatchStatus;

@Data
@Builder
public class MatchResponse {
    private UUID idMatch;
    private UUID requesterId;
    private UUID targetId;
    private MatchStatus status;
    private AffinityScoreResponse affinityScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
