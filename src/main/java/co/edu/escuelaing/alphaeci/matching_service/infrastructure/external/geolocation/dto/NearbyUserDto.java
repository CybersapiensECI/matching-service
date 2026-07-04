package co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.geolocation.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearbyUserDto {
    private UUID userId;
    private double distanceMeters;
    private String zona;
}