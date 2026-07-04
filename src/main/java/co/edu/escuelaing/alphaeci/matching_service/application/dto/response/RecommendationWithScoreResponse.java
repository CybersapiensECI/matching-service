package co.edu.escuelaing.alphaeci.matching_service.application.dto.response;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Recommended user with affinity score breakdown")
public class RecommendationWithScoreResponse {

    @Schema(description = "ID of the recommended user")
    private UUID targetUserId;

    @Schema(description = "Overall affinity score (0.0 - 1.0)")
    private double totalScore;

    @Schema(description = "Score based on shared interests")
    private double interestScore;

    @Schema(description = "Score based on academic profile")
    private double academicScore;

    @Schema(description = "Score based on schedule compatibility")
    private double scheduleScore;
}
