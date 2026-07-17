package co.edu.escuelaing.alphaeci.matching_service.application.dto.response;

import co.edu.escuelaing.alphaeci.matching_service.domain.model.Relationship;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class RelationshipResponse {
    private Relationship.RelationshipStatus status;
    private UUID matchId;

    public static RelationshipResponse from(Relationship relationship) {
        return new RelationshipResponse(relationship.getStatus(), relationship.getMatchId());
    }
}
