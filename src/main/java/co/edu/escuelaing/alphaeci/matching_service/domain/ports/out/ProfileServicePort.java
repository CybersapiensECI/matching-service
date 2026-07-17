package co.edu.escuelaing.alphaeci.matching_service.domain.ports.out;

import java.util.List;
import java.util.UUID;

import co.edu.escuelaing.alphaeci.matching_service.domain.model.MatchProfile;

public interface ProfileServicePort {
    MatchProfile getProfileById(UUID userId);
    List<MatchProfile> getAllProfiles();
    List<MatchProfile> getAllProfiles(UUID excludeUserId);
    List<UUID> getFriends(UUID userId);
    void addFriend(UUID userId, UUID friendId);
    void removeFriend(UUID userId, UUID friendId);
    boolean isGeolocationEnabled(UUID userId);
    boolean isActive(UUID userId);
}
