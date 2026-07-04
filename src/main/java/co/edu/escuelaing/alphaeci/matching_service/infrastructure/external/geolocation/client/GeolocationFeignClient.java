package co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.geolocation.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.geolocation.dto.NearbyUserDto;

@FeignClient(
    name = "geolocation-service",
    url = "${GEOLOCATION_SERVICE_URL}",
    path = "${GEOLOCATION_SERVICE_PATH}"
)
public interface GeolocationFeignClient {

    @GetMapping("/geolocation/nearby")
    List<NearbyUserDto> getNearbyUsers(@RequestParam UUID userId, @RequestParam Double radius, @RequestParam Boolean soloActivos);

}