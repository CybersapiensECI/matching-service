package co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.profile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import co.edu.escuelaing.alphaeci.matching_service.domain.exceptions.ExternalServiceException;
import co.edu.escuelaing.alphaeci.matching_service.domain.exceptions.NotFoundException;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.MatchProfile;
import co.edu.escuelaing.alphaeci.matching_service.domain.ports.out.ProfileServicePort;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.profile.client.ProfileFeignClient;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.profile.client.ProfilePublicFeignClient;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.profile.dto.FriendRequestDto;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.profile.dto.UserMatchProfileDto;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileServiceAdapter implements ProfileServicePort {

    private final ProfileFeignClient profileFeignClient;
    private final ProfilePublicFeignClient profilePublicFeignClient;

    @Override
    public MatchProfile getProfileById(UUID userId) {
        try {
            return toMatchProfile(profileFeignClient.getProfileById(userId))
                    .orElseThrow(() -> new NotFoundException("Profile not found with ID: " + userId));
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Profile not found with ID: " + userId);
        } catch (FeignException e) {
            throw new ExternalServiceException("Profile service unavailable: " + e.getMessage());
        }
    }

    @Override
    public List<MatchProfile> getAllProfiles() {
        try {
            return profileFeignClient.getAllProfiles().stream()
                    .map(this::toMatchProfile)
                    .flatMap(Optional::stream)
                    .toList();
        } catch (FeignException e) {
            throw new ExternalServiceException("Profile service unavailable: " + e.getMessage());
        }
    }

    @Override
    public List<MatchProfile> getAllProfiles(UUID excludeUserId) {
        try {
            return profileFeignClient.getAllProfiles(excludeUserId).stream()
                    .map(this::toMatchProfile)
                    .flatMap(Optional::stream)
                    .toList();
        } catch (FeignException e) {
            throw new ExternalServiceException("Profile service unavailable: " + e.getMessage());
        }
    }

    @Override
    public List<UUID> getFriends(UUID userId) {
        try {
            return profilePublicFeignClient.getFriends(userId);
        } catch (FeignException e) {
            throw new ExternalServiceException("Profile service unavailable: " + e.getMessage());
        }
    }

    @Override
    public void addFriend(UUID userId, UUID friendId) {
        try {
            profilePublicFeignClient.addFriend(userId, new FriendRequestDto(friendId));
        } catch (FeignException e) {
            throw new ExternalServiceException("Profile service unavailable while adding friend: " + e.getMessage());
        }
    }

    @Override
    public boolean isGeolocationEnabled(UUID userId) {
        try {
            return Boolean.TRUE.equals(profileFeignClient.isGeolocationEnabled(userId));
        } catch (FeignException e) {
            throw new ExternalServiceException("Profile service unavailable: " + e.getMessage());
        }
    }

    @Override
    public boolean isActive(UUID userId) {
        try {
            return Boolean.TRUE.equals(profileFeignClient.isActive(userId));
        } catch (FeignException e) {
            throw new ExternalServiceException("Profile service unavailable: " + e.getMessage());
        }
    }

    private Optional<MatchProfile> toMatchProfile(UserMatchProfileDto dto) {
        UUID id;
        try {
            id = UUID.fromString(dto.getId());
        } catch (IllegalArgumentException | NullPointerException e) {
            log.warn("Skipping profile with non-UUID id from profile-service: {}", dto.getId());
            return Optional.empty();
        }
        return Optional.of(new MatchProfile(
                id,
                dto.getCareer(),
                dto.getSemester(),
                dto.getTags(),
                dto.getSchedulesAvailable(),
                dto.isActive()
        ));
    }
}
