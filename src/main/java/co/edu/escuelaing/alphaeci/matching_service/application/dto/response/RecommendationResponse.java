package co.edu.escuelaing.alphaeci.matching_service.application.dto.response;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecommendationResponse {
    private UUID userId;
    private List<UUID> recommendedUserIds;
}
