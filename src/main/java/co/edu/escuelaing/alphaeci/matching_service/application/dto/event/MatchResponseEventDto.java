package co.edu.escuelaing.alphaeci.matching_service.application.dto.event;

import java.time.LocalDateTime;
import java.util.UUID;

import co.edu.escuelaing.alphaeci.matching_service.domain.model.enums.MatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponseEventDto {
    private UUID senderUserId;
    private UUID receiverUserId;
    private MatchStatus status;
    private LocalDateTime respondedAt;
}
