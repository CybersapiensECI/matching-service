package co.edu.escuelaing.alphaeci.matching_service.application.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AffinityScoreResponse {
    private double score;
    private double interestScore;
    private double academicScore;
    private double scheduleScore;
}
