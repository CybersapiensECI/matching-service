package co.edu.escuelaing.alphaeci.matching_service.application.dto.event;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchReceivedEventDto {
    private UUID senderUserId;
    private UUID receiverUserId;
    private Double affinityPercentage;
    private LocalDateTime createdAt;
}
