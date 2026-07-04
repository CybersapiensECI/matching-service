package co.edu.escuelaing.alphaeci.matching_service.application.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

@Data
public class AffinityScoreUpdateRequest {

    @DecimalMin(value = "0.0", message = "The score cannot be negative")
    @DecimalMax(value = "1.0", message = "The score cannot be greater than 1")
    private Double score;

    @DecimalMin(value = "0.0", message = "The interest score cannot be negative")
    @DecimalMax(value = "1.0", message = "The interest score cannot be greater than 1")
    private Double interestScore;

    @DecimalMin(value = "0.0", message = "The schedule score cannot be negative")
    @DecimalMax(value = "1.0", message = "The schedule score cannot be greater than 1")
    private Double scheduleScore;
}
