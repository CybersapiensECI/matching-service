package co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.geolocation;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import co.edu.escuelaing.alphaeci.matching_service.domain.exceptions.ExternalServiceException;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.NearbyUserDistance;
import co.edu.escuelaing.alphaeci.matching_service.domain.ports.out.GeolocationServicePort;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.geolocation.client.GeolocationFeignClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GeolocationServiceAdapter implements GeolocationServicePort {

	private final GeolocationFeignClient geolocationFeignClient;

	@Override
	public List<NearbyUserDistance> getNearbyUsers(UUID userId) {
		try {
			return geolocationFeignClient.getNearbyUsers(userId, 200.0, true).stream()
					.map(dto -> new NearbyUserDistance(dto.getUserId(), dto.getDistanceMeters()))
					.toList();
		} catch (FeignException e) {
			throw new ExternalServiceException("Geolocation service unavailable: " + e.getMessage());
		}
	}

	@Override
	public List<UUID> getNearbyUserIds(UUID userId) {
		return getNearbyUsers(userId).stream()
				.map(NearbyUserDistance::getUserId)
				.toList();
	}

}
