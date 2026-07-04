package co.edu.escuelaing.alphaeci.matching_service.domain.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

import co.edu.escuelaing.alphaeci.matching_service.domain.model.enums.MatchStatus;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.AffinityScore;

@Data
public class Match {
    private UUID idMatch;
    private UUID requesterId;
    private UUID targetId;
    private MatchStatus status;
    private AffinityScore affinityScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
