package co.edu.escuelaing.alphaeci.matching_service.domain.model;

import lombok.Data;

import java.util.UUID;

@Data
public class Relationship {
    private RelationshipStatus status;
    private UUID matchId;

    public enum RelationshipStatus {
        FRIEND,
        PENDING_SENT,
        PENDING_RECEIVED,
        NONE
    }

    public static Relationship of(RelationshipStatus status, UUID matchId) {
        Relationship r = new Relationship();
        r.setStatus(status);
        r.setMatchId(matchId);
        return r;
    }
}
