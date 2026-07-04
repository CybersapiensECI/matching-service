package co.edu.escuelaing.alphaeci.matching_service.domain.ports.out;

import java.util.List;
import java.util.UUID;

import co.edu.escuelaing.alphaeci.matching_service.domain.model.NearbyUserDistance;


public interface GeolocationServicePort {

	List<NearbyUserDistance> getNearbyUsers(UUID userId);

	List<UUID> getNearbyUserIds(UUID userId);

}
