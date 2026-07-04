package co.edu.escuelaing.alphaeci.matching_service.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class MatchRequest {

    @NotNull(message = "The requester ID is required")
    private UUID requesterId;

    @NotNull(message = "The target ID is required")
    private UUID targetId;
}
