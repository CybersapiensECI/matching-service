package co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AffinityScoreDocument {
    private double totalScore;
    private double interestScore;
    private double academicScore;
    private double scheduleScore;
}
