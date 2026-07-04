package co.edu.escuelaing.alphaeci.matching_service.domain.ports.out;

import java.util.UUID;

import co.edu.escuelaing.alphaeci.matching_service.domain.model.enums.MatchStatus;

public interface MatchEventPublisherPort {

    void publishMatchReceived(UUID senderUserId, UUID receiverUserId, Double affinityPercentage);
    void publishMatchResponse(UUID senderUserId, UUID receiverUserId, MatchStatus status);
}
