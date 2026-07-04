package co.edu.escuelaing.alphaeci.matching_service.application.dto.request;

import co.edu.escuelaing.alphaeci.matching_service.domain.model.enums.MatchStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MatchUpdateRequest {

    @NotNull(message = "The match status must not be null")
    private MatchStatus status;
}
