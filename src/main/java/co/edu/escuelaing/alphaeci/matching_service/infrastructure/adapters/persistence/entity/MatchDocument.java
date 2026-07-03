package co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import co.edu.escuelaing.alphaeci.matching_service.domain.model.enums.MatchStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchDocument {
    @Id
    private UUID idMatch;
    private UUID requesterId;
    private UUID targetId;
    private MatchStatus status;
    private AffinityScoreDocument affinityScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
