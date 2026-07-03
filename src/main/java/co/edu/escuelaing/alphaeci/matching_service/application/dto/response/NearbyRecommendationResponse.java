package co.edu.escuelaing.alphaeci.matching_service.application.dto.response;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Recommended nearby user with affinity score breakdown and distance")
public class NearbyRecommendationResponse {

    @Schema(description = "ID of the recommended user")
    private UUID targetUserId;

    @Schema(description = "Distance from the requester in meters")
    private double distanceMeters;

    @Schema(description = "Overall affinity score (0.0 - 1.0)")
    private double totalScore;

    @Schema(description = "Score based on shared interests")
    private double interestScore;

    @Schema(description = "Score based on academic profile")
    private double academicScore;

    @Schema(description = "Score based on schedule compatibility")
    private double scheduleScore;
}