package co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.profile.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import co.edu.escuelaing.alphaeci.matching_service.infrastructure.config.InternalFeignConfig;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.profile.dto.FriendRequestDto;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.profile.dto.UserMatchProfileDto;

@FeignClient(
    name = "profile-service",
    contextId = "profileInternal",
    url = "${PROFILE_SERVICE_URL}",
    path = "${PROFILE_SERVICE_PATH}",
    configuration = InternalFeignConfig.class
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

    /**
     * Bajo /api/v1/internal (InternalFeignConfig manda X-Internal-Key): a
     * diferencia de la version publica bajo /api/v1/users, esta no exige un
     * JWT de usuario, que ninguna de las dos partes del match tiene sobre la
     * otra.
     */
    @GetMapping("/users/{userId}/friends")
    List<UUID> getFriends(@PathVariable UUID userId);

    @PostMapping("/users/{userId}/friends")
    void addFriend(@PathVariable UUID userId, @RequestBody FriendRequestDto request);

    @DeleteMapping("/users/{userId}/friends/{friendId}")
    void removeFriend(@PathVariable UUID userId, @PathVariable UUID friendId);
}
