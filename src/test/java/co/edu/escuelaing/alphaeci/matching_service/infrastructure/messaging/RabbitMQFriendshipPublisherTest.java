package co.edu.escuelaing.alphaeci.matching_service.infrastructure.messaging;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import co.edu.escuelaing.alphaeci.matching_service.application.dto.event.MatchReceivedEventDto;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.event.MatchResponseEventDto;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.enums.MatchStatus;

class RabbitMQFriendshipPublisherTest {

    @Mock
    RabbitTemplate rabbitTemplate;

    RabbitMQFriendshipPublisher publisher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        publisher = new RabbitMQFriendshipPublisher(rabbitTemplate);
    }

    @Test
    void publishMatchReceived_sendsEventToMatchingExchange() {
        UUID sender = UUID.randomUUID();
        UUID receiver = UUID.randomUUID();

        publisher.publishMatchReceived(sender, receiver, 0.85);

        var captor = org.mockito.ArgumentCaptor.forClass(MatchReceivedEventDto.class);
        verify(rabbitTemplate).convertAndSend(eq("matching.exchange"), eq("match.received"), captor.capture());

        MatchReceivedEventDto event = captor.getValue();
        assertEqualsAll(sender, receiver, event.getSenderUserId(), event.getReceiverUserId());
        org.junit.jupiter.api.Assertions.assertEquals(0.85, event.getAffinityPercentage());
        org.junit.jupiter.api.Assertions.assertNotNull(event.getCreatedAt());
    }

    @Test
    void publishMatchResponse_sendsEventToMatchingExchange() {
        UUID sender = UUID.randomUUID();
        UUID receiver = UUID.randomUUID();

        publisher.publishMatchResponse(sender, receiver, MatchStatus.ACCEPTED);

        var captor = org.mockito.ArgumentCaptor.forClass(MatchResponseEventDto.class);
        verify(rabbitTemplate).convertAndSend(eq("matching.exchange"), eq("match.response"), captor.capture());

        MatchResponseEventDto event = captor.getValue();
        assertEqualsAll(sender, receiver, event.getSenderUserId(), event.getReceiverUserId());
        org.junit.jupiter.api.Assertions.assertEquals(MatchStatus.ACCEPTED, event.getStatus());
        org.junit.jupiter.api.Assertions.assertNotNull(event.getRespondedAt());
    }

    private void assertEqualsAll(UUID expectedSender, UUID expectedReceiver, UUID actualSender, UUID actualReceiver) {
        org.junit.jupiter.api.Assertions.assertEquals(expectedSender, actualSender);
        org.junit.jupiter.api.Assertions.assertEquals(expectedReceiver, actualReceiver);
    }
}
