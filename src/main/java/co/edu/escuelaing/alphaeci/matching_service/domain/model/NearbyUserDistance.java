package co.edu.escuelaing.alphaeci.matching_service.domain.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearbyUserDistance {

    private UUID userId;
    private double distanceMeters;
}