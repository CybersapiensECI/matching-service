package co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects;

import lombok.Data;

@Data
public class AffinityScore {
    private double totalScore;
    private double interestScore;
    private double academicScore;
    private double scheduleScore;
}
