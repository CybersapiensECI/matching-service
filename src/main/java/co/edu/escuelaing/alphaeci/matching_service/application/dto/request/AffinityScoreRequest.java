package co.edu.escuelaing.alphaeci.matching_service.application.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AffinityScoreRequest {

    @NotNull(message = "The total score is required")
    @DecimalMin(value = "0.0", message = "The score cannot be negative")
    @DecimalMax(value = "1.0", message = "The score cannot be greater than 1")
    private Double score;

    @NotNull(message = "The interest score is required")
    @DecimalMin(value = "0.0", message = "The interest score cannot be negative")
    @DecimalMax(value = "1.0", message = "The interest score cannot be greater than 1")
    private Double interestScore;

    @NotNull(message = "The schedule score is required")
    @DecimalMin(value = "0.0", message = "The schedule score cannot be negative")
    @DecimalMax(value = "1.0", message = "The schedule score cannot be greater than 1")
    private Double scheduleScore;
}
