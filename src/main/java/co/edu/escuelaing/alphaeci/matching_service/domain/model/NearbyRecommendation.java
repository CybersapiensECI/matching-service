package co.edu.escuelaing.alphaeci.matching_service.domain.model;

import java.util.UUID;

import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.AffinityScore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearbyRecommendation {

    private NearbyUserDistance nearbyUserDistance;
    private AffinityScore affinityScore;

    public UUID getUserId() {
        return nearbyUserDistance.getUserId();
    }

    public double getDistanceMeters() {
        return nearbyUserDistance.getDistanceMeters();
    }
}