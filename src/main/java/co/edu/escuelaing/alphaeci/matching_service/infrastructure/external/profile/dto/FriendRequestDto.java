package co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class FriendRequestDto {
    private UUID friendId;
}
