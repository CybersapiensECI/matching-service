package co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.profile.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.profile.dto.UserMatchProfileDto;

@FeignClient(
    name = "profile-service",
    contextId = "profileInternal",
    url = "${PROFILE_SERVICE_URL}",
    path = "${PROFILE_SERVICE_PATH}"
)
public interface ProfileFeignClient {

    @GetMapping("/matching/profiles/{id}")
    UserMatchProfileDto getProfileById(@PathVariable UUID id);

    @GetMapping("/matching/profiles")
    List<UserMatchProfileDto> getAllProfiles();

    @GetMapping("/matching/profiles/candidates/{userId}")
    List<UserMatchProfileDto> getAllProfiles(@PathVariable UUID userId);

    @GetMapping("/users/{userId}/geolocation")
    Boolean isGeolocationEnabled(@PathVariable UUID userId);

    @GetMapping("/users/{userId}/active")
    Boolean isActive(@PathVariable UUID userId);
}
