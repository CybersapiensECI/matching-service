package co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.profile.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.profile.dto.FriendRequestDto;


@FeignClient(
    name = "profile-service",
    contextId = "profilePublic",
    url = "${PROFILE_SERVICE_URL}",
    path = "${PROFILE_SERVICE_PUBLIC_PATH}"
)
public interface ProfilePublicFeignClient {

    @GetMapping("/users/{userId}/friends")
    List<UUID> getFriends(@PathVariable UUID userId);

    @PostMapping("/users/{userId}/friends")
    void addFriend(@PathVariable UUID userId, @RequestBody FriendRequestDto request);
}
