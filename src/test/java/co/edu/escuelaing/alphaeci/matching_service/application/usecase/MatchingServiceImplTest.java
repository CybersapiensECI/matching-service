package co.edu.escuelaing.alphaeci.matching_service.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.escuelaing.alphaeci.matching_service.domain.exceptions.ExternalServiceException;
import co.edu.escuelaing.alphaeci.matching_service.domain.exceptions.InvalidInputException;
import co.edu.escuelaing.alphaeci.matching_service.domain.exceptions.NotFoundException;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.Match;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.MatchProfile;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.Relationship;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.enums.MatchStatus;
import co.edu.escuelaing.alphaeci.matching_service.domain.ports.in.RecommendationsUseCasePort;
import co.edu.escuelaing.alphaeci.matching_service.domain.ports.out.MatchEventPublisherPort;
import co.edu.escuelaing.alphaeci.matching_service.domain.ports.out.MatchRepositoryPort;
import co.edu.escuelaing.alphaeci.matching_service.domain.ports.out.ProfileServicePort;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.AffinityScore;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingServiceImplTest {

    @Mock
    private MatchRepositoryPort matchRepository;

    @Mock
    private RecommendationsUseCasePort recommendationsUseCase;

    @Mock
    private ProfileServicePort profileServicePort;

    @Mock
    private MatchEventPublisherPort matchEventPublisher;

    @InjectMocks
    private MatchingServiceImpl matchingService;

    private UUID requesterId;
    private UUID targetId;
    private UUID matchId;
    private Match pendingMatch;

    @BeforeEach
    void setUp() {
        requesterId = UUID.randomUUID();
        targetId = UUID.randomUUID();
        matchId = UUID.randomUUID();

        pendingMatch = new Match();
        pendingMatch.setIdMatch(matchId);
        pendingMatch.setRequesterId(requesterId);
        pendingMatch.setTargetId(targetId);
        pendingMatch.setStatus(MatchStatus.PENDING);
        pendingMatch.setAffinityScore(new AffinityScore());
        pendingMatch.setCreatedAt(LocalDateTime.now());
        pendingMatch.setUpdatedAt(LocalDateTime.now());
    }

    // ======================== CREATE MATCH ========================

    @Test
    void createMatch_success() {
        MatchProfile profile = new MatchProfile(requesterId, "Systems", 4, List.of("java"), List.of("monday-morning"), true);
        when(profileServicePort.getProfileById(requesterId)).thenReturn(profile);
        when(profileServicePort.getFriends(requesterId)).thenReturn(List.of());
        when(matchRepository.findByRequesterId(requesterId)).thenReturn(List.of());
        when(matchRepository.findByRequesterId(targetId)).thenReturn(List.of());
        when(recommendationsUseCase.calculateAffinityScore(requesterId, targetId)).thenReturn(pendingMatch.getAffinityScore());
        when(matchRepository.save(any(Match.class))).thenReturn(pendingMatch);

        Match result = matchingService.createMatch(requesterId, targetId);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(MatchStatus.PENDING);

        ArgumentCaptor<Match> captor = ArgumentCaptor.forClass(Match.class);
        verify(matchRepository).save(captor.capture());
        Match saved = captor.getValue();
        assertThat(saved.getRequesterId()).isEqualTo(requesterId);
        assertThat(saved.getTargetId()).isEqualTo(targetId);
        assertThat(saved.getIdMatch()).isNotNull();

        verify(matchEventPublisher).publishMatchReceived(requesterId, targetId, pendingMatch.getAffinityScore().getTotalScore());
    }

    @Test
    void createMatch_sameUser_throwsInvalidInputException() {
        assertThatThrownBy(() -> matchingService.createMatch(requesterId, requesterId))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("yourself");
    }

    @Test
    void createMatch_noTags_throwsInvalidInputException() {
        MatchProfile profile = new MatchProfile(requesterId, "Systems", 4, List.of(), List.of("monday-morning"), true);
        when(profileServicePort.getProfileById(requesterId)).thenReturn(profile);

        assertThatThrownBy(() -> matchingService.createMatch(requesterId, targetId))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("tags");
    }

    @Test
    void createMatch_noSchedules_throwsInvalidInputException() {
        MatchProfile profile = new MatchProfile(requesterId, "Systems", 4, List.of("java"), List.of(), true);
        when(profileServicePort.getProfileById(requesterId)).thenReturn(profile);

        assertThatThrownBy(() -> matchingService.createMatch(requesterId, targetId))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("schedules");
    }

    @Test
    void createMatch_targetIsFriend_throwsInvalidInputException() {
        MatchProfile profile = new MatchProfile(requesterId, "Systems", 4, List.of("java"), List.of("monday-morning"), true);
        when(profileServicePort.getProfileById(requesterId)).thenReturn(profile);
        when(profileServicePort.getFriends(requesterId)).thenReturn(List.of(targetId));

        assertThatThrownBy(() -> matchingService.createMatch(requesterId, targetId))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("already your friend");
    }

    @Test
    void createMatch_alreadyExists_throwsInvalidInputException() {
        MatchProfile profile = new MatchProfile(requesterId, "Systems", 4, List.of("java"), List.of("monday-morning"), true);
        when(profileServicePort.getProfileById(requesterId)).thenReturn(profile);
        when(profileServicePort.getFriends(requesterId)).thenReturn(List.of());
        when(matchRepository.findByRequesterId(requesterId)).thenReturn(List.of(pendingMatch));

        assertThatThrownBy(() -> matchingService.createMatch(requesterId, targetId))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Already exists a match request");
    }

    @Test
    void createMatch_pendingFromOtherDirection_throwsInvalidInputException() {
        // El PENDING existente lo mandó el target hacia el requester (dirección
        // contraria) — antes existsByRequesterIdAndTargetId(requesterId, targetId)
        // no lo veía y dejaba crear un segundo documento para el mismo par.
        Match reversePending = new Match();
        reversePending.setIdMatch(UUID.randomUUID());
        reversePending.setRequesterId(targetId);
        reversePending.setTargetId(requesterId);
        reversePending.setStatus(MatchStatus.PENDING);

        MatchProfile profile = new MatchProfile(requesterId, "Systems", 4, List.of("java"), List.of("monday-morning"), true);
        when(profileServicePort.getProfileById(requesterId)).thenReturn(profile);
        when(profileServicePort.getFriends(requesterId)).thenReturn(List.of());
        when(matchRepository.findByRequesterId(requesterId)).thenReturn(List.of());
        when(matchRepository.findByRequesterId(targetId)).thenReturn(List.of(reversePending));

        assertThatThrownBy(() -> matchingService.createMatch(requesterId, targetId))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Already exists a match request");
    }

    @Test
    void createMatch_rejectedExists_deletesStaleDocAndCreatesNew() {
        Match rejected = new Match();
        rejected.setIdMatch(UUID.randomUUID());
        rejected.setRequesterId(requesterId);
        rejected.setTargetId(targetId);
        rejected.setStatus(MatchStatus.REJECTED);

        MatchProfile profile = new MatchProfile(requesterId, "Systems", 4, List.of("java"), List.of("monday-morning"), true);
        when(profileServicePort.getProfileById(requesterId)).thenReturn(profile);
        when(profileServicePort.getFriends(requesterId)).thenReturn(List.of());
        when(matchRepository.findByRequesterId(requesterId)).thenReturn(List.of(rejected));
        when(recommendationsUseCase.calculateAffinityScore(requesterId, targetId)).thenReturn(pendingMatch.getAffinityScore());
        when(matchRepository.save(any(Match.class))).thenReturn(pendingMatch);

        Match result = matchingService.createMatch(requesterId, targetId);

        assertThat(result.getStatus()).isEqualTo(MatchStatus.PENDING);
        verify(matchRepository).delete(rejected.getIdMatch());
        verify(matchRepository).save(any(Match.class));
    }

    // ======================== GET MATCH ========================

    @Test
    void getMatch_success() {
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(pendingMatch));

        Match result = matchingService.getMatch(matchId);

        assertThat(result).isEqualTo(pendingMatch);
    }

    @Test
    void getMatch_notFound_throwsNotFoundException() {
        when(matchRepository.findById(matchId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchingService.getMatch(matchId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Match not found");
    }

    // ======================== FIND BY IDS ========================

    @Test
    void findByRequesterId_returnsList() {
        when(matchRepository.findByRequesterId(requesterId)).thenReturn(List.of(pendingMatch));

        List<Match> result = matchingService.findByRequesterId(requesterId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(pendingMatch);
    }

    @Test
    void findByTargetId_returnsList() {
        when(matchRepository.findByTargetId(targetId)).thenReturn(List.of(pendingMatch));

        List<Match> result = matchingService.findByTargetId(targetId);

        assertThat(result).hasSize(1);
    }

    // ======================== RESPOND TO MATCH REQUEST ========================

    @Test
    void respondToMatchRequest_accept_setsAcceptedAndAddsFriend() {
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(pendingMatch));
        when(matchRepository.save(any(Match.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(profileServicePort).addFriend(any(UUID.class), any(UUID.class));

        Match result = matchingService.respondToMatchRequest(matchId, targetId, true);

        assertThat(result.getStatus()).isEqualTo(MatchStatus.ACCEPTED);
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(profileServicePort).addFriend(requesterId, targetId);
        verify(matchEventPublisher).publishMatchResponse(requesterId, targetId, MatchStatus.ACCEPTED);
    }

    @Test
    void respondToMatchRequest_accept_throwsWhenProfileServiceFails() {
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(pendingMatch));
        doThrow(new RuntimeException("timeout")).when(profileServicePort).addFriend(any(UUID.class), any(UUID.class));

        assertThatThrownBy(() -> matchingService.respondToMatchRequest(matchId, targetId, true))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("Cannot accept match right now");

        verify(matchRepository, never()).save(any());
    }

    @Test
    void respondToMatchRequest_reject_setsRejected() {
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(pendingMatch));
        when(matchRepository.save(any(Match.class))).thenAnswer(inv -> inv.getArgument(0));

        Match result = matchingService.respondToMatchRequest(matchId, targetId, false);

        assertThat(result.getStatus()).isEqualTo(MatchStatus.REJECTED);
        verify(matchEventPublisher).publishMatchResponse(requesterId, targetId, MatchStatus.REJECTED);
    }

    @Test
    void respondToMatchRequest_wrongResponder_throwsInvalidInputException() {
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(pendingMatch));
        UUID wrongUser = UUID.randomUUID();

        assertThatThrownBy(() -> matchingService.respondToMatchRequest(matchId, wrongUser, true))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Only the recipient");
    }

    @Test
    void respondToMatchRequest_notPending_throwsInvalidInputException() {
        pendingMatch.setStatus(MatchStatus.ACCEPTED);
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(pendingMatch));

        assertThatThrownBy(() -> matchingService.respondToMatchRequest(matchId, targetId, true))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Only pending requests can be responded to");
    }

    @Test
    void respondToMatchRequest_notFound_throwsNotFoundException() {
        when(matchRepository.findById(matchId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchingService.respondToMatchRequest(matchId, targetId, true))
                .isInstanceOf(NotFoundException.class);
    }

    // ======================== CANCEL MATCH ========================

    @Test
    void cancelMatch_success() {
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(pendingMatch));
        doNothing().when(matchRepository).delete(matchId);

        matchingService.cancelMatch(matchId, requesterId);

        verify(matchRepository).delete(matchId);
    }

    @Test
    void cancelMatch_wrongRequester_throwsInvalidInputException() {
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(pendingMatch));
        UUID wrongUser = UUID.randomUUID();

        assertThatThrownBy(() -> matchingService.cancelMatch(matchId, wrongUser))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Only the sender");
    }

    @Test
    void cancelMatch_notPending_throwsInvalidInputException() {
        pendingMatch.setStatus(MatchStatus.ACCEPTED);
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(pendingMatch));

        assertThatThrownBy(() -> matchingService.cancelMatch(matchId, requesterId))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Only pending match requests can be cancelled");
    }

    @Test
    void cancelMatch_notFound_throwsNotFoundException() {
        when(matchRepository.findById(matchId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchingService.cancelMatch(matchId, requesterId))
                .isInstanceOf(NotFoundException.class);
    }

    // ======================== RELATIONSHIP ========================

    @Test
    void getRelationship_alreadyFriends_returnsFriend() {
        when(profileServicePort.getFriends(requesterId)).thenReturn(List.of(targetId));

        Relationship result = matchingService.getRelationship(requesterId, targetId);

        assertThat(result.getStatus()).isEqualTo(Relationship.RelationshipStatus.FRIEND);
        assertThat(result.getMatchId()).isNull();
        verify(matchRepository, never()).findByRequesterId(any());
    }

    @Test
    void getRelationship_pendingSent_returnsPendingSent() {
        when(profileServicePort.getFriends(requesterId)).thenReturn(List.of());
        when(matchRepository.findByRequesterId(requesterId)).thenReturn(List.of(pendingMatch));

        Relationship result = matchingService.getRelationship(requesterId, targetId);

        assertThat(result.getStatus()).isEqualTo(Relationship.RelationshipStatus.PENDING_SENT);
        assertThat(result.getMatchId()).isEqualTo(matchId);
    }

    @Test
    void getRelationship_pendingReceived_returnsPendingReceived() {
        // Lo mandó el otro usuario hacia el userId que pregunta.
        Match receivedMatch = new Match();
        receivedMatch.setIdMatch(UUID.randomUUID());
        receivedMatch.setRequesterId(targetId);
        receivedMatch.setTargetId(requesterId);
        receivedMatch.setStatus(MatchStatus.PENDING);

        when(profileServicePort.getFriends(requesterId)).thenReturn(List.of());
        when(matchRepository.findByRequesterId(requesterId)).thenReturn(List.of());
        when(matchRepository.findByTargetId(requesterId)).thenReturn(List.of(receivedMatch));

        Relationship result = matchingService.getRelationship(requesterId, targetId);

        assertThat(result.getStatus()).isEqualTo(Relationship.RelationshipStatus.PENDING_RECEIVED);
        assertThat(result.getMatchId()).isEqualTo(receivedMatch.getIdMatch());
    }

    @Test
    void getRelationship_nothing_returnsNone() {
        when(profileServicePort.getFriends(requesterId)).thenReturn(List.of());
        when(matchRepository.findByRequesterId(requesterId)).thenReturn(List.of());
        when(matchRepository.findByTargetId(requesterId)).thenReturn(List.of());

        Relationship result = matchingService.getRelationship(requesterId, targetId);

        assertThat(result.getStatus()).isEqualTo(Relationship.RelationshipStatus.NONE);
    }

    // ======================== REMOVE FRIEND ========================

    @Test
    void removeFriend_success_deletesAcceptedMatchBetweenThem() {
        pendingMatch.setStatus(MatchStatus.ACCEPTED);
        when(matchRepository.findByRequesterId(requesterId)).thenReturn(List.of(pendingMatch));

        matchingService.removeFriend(requesterId, targetId);

        verify(profileServicePort).removeFriend(requesterId, targetId);
        verify(matchRepository).delete(pendingMatch.getIdMatch());
    }

    @Test
    void removeFriend_noAcceptedMatch_doesNotDeleteAnything() {
        when(matchRepository.findByRequesterId(requesterId)).thenReturn(List.of());
        when(matchRepository.findByRequesterId(targetId)).thenReturn(List.of());

        matchingService.removeFriend(requesterId, targetId);

        verify(profileServicePort).removeFriend(requesterId, targetId);
        verify(matchRepository, never()).delete(any());
    }

    @Test
    void removeFriend_profileServiceFails_throwsExternalServiceException() {
        doThrow(new RuntimeException("down")).when(profileServicePort).removeFriend(requesterId, targetId);

        assertThatThrownBy(() -> matchingService.removeFriend(requesterId, targetId))
                .isInstanceOf(ExternalServiceException.class);
    }
}
