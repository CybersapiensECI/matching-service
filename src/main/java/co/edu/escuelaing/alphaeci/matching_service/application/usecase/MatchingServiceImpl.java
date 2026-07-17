package co.edu.escuelaing.alphaeci.matching_service.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import co.edu.escuelaing.alphaeci.matching_service.domain.exceptions.ExternalServiceException;
import co.edu.escuelaing.alphaeci.matching_service.domain.exceptions.InvalidInputException;
import co.edu.escuelaing.alphaeci.matching_service.domain.exceptions.NotFoundException;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.Match;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.Relationship;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.enums.MatchStatus;
import co.edu.escuelaing.alphaeci.matching_service.domain.ports.in.MatchUseCasePort;
import co.edu.escuelaing.alphaeci.matching_service.domain.ports.in.RecommendationsUseCasePort;
import co.edu.escuelaing.alphaeci.matching_service.domain.ports.out.MatchEventPublisherPort;
import co.edu.escuelaing.alphaeci.matching_service.domain.ports.out.MatchRepositoryPort;
import co.edu.escuelaing.alphaeci.matching_service.domain.ports.out.ProfileServicePort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchUseCasePort {

    private final MatchRepositoryPort matchRepository;
    private final RecommendationsUseCasePort recommendationsUseCase;
    private final ProfileServicePort profileServicePort;
    private final MatchEventPublisherPort matchEventPublisher;

    @Override
    public Match createMatch(UUID requesterId, UUID targetId) {
        if (requesterId.equals(targetId)) {
            throw new InvalidInputException("Cannot send a match request to yourself");
        }

        var requesterProfile = profileServicePort.getProfileById(requesterId);
        if (requesterProfile.getTags() == null || requesterProfile.getTags().isEmpty()) {
            throw new InvalidInputException("You can't match without any tags!");
        }
        if (requesterProfile.getSchedulesAvailable() == null || requesterProfile.getSchedulesAvailable().isEmpty()) {
            throw new InvalidInputException("You can't match without any available schedules!");
        }

        List<UUID> friends = profileServicePort.getFriends(requesterId);
        if (friends.contains(targetId)) {
            throw new InvalidInputException("Cannot send a match request to someone who is already your friend");
        }

        // Bidireccional a propósito: existsByRequesterIdAndTargetId(requesterId, targetId)
        // solo miraba una dirección, así que A podía volver a pedirle a B por el otro
        // lado incluso con un match PENDING/ACCEPTED ya abierto de B hacia A, dejando
        // dos documentos de match independientes para el mismo par (visible como
        // "Rechazada" en Enviadas para una pareja que en realidad ya es amiga).
        Optional<Match> existing = findExistingBetween(requesterId, targetId);
        if (existing.isPresent()) {
            Match match = existing.get();
            if (match.getStatus() == MatchStatus.PENDING) {
                throw new InvalidInputException("Already exists a match request between requester and target");
            }
            if (match.getStatus() == MatchStatus.ACCEPTED) {
                throw new InvalidInputException("Cannot send a match request to someone who is already your friend");
            }
            // REJECTED: se limpia para permitir un intento nuevo sin dejar el
            // documento viejo dando vueltas para siempre.
            matchRepository.delete(match.getIdMatch());
        }

        Match match = new Match();
        match.setIdMatch(UUID.randomUUID());
        match.setRequesterId(requesterId);
        match.setTargetId(targetId);
        match.setStatus(MatchStatus.PENDING);
        match.setCreatedAt(LocalDateTime.now());
        match.setUpdatedAt(LocalDateTime.now());

        match.setAffinityScore(recommendationsUseCase.calculateAffinityScore(requesterId, targetId));

        Match saved = matchRepository.save(match);
        matchEventPublisher.publishMatchReceived(requesterId, targetId, saved.getAffinityScore().getTotalScore());
        return saved;
    }

    @Override
    public Match getMatch(UUID matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Match not found with ID: " + matchId));
    }

    @Override
    public List<Match> findByRequesterId(UUID requesterId) {
        return matchRepository.findByRequesterId(requesterId);
    }

    @Override
    public List<Match> findByTargetId(UUID targetId) {
        return matchRepository.findByTargetId(targetId);
    }

    @Override
    public Match respondToMatchRequest(UUID matchId, UUID responderId, boolean accept) {
        Match match = getMatch(matchId);
        if (!match.getTargetId().equals(responderId)) {
            throw new InvalidInputException("Only the recipient of a match request can accept or reject it");
        }
        if (match.getStatus() != MatchStatus.PENDING) {
            throw new InvalidInputException("Only pending requests can be responded to");
        }
        match.setStatus(accept ? MatchStatus.ACCEPTED : MatchStatus.REJECTED);
        match.setUpdatedAt(LocalDateTime.now());

        if (accept) {
            try {
                profileServicePort.addFriend(match.getRequesterId(), match.getTargetId());
            } catch (Exception e) {
                log.warn("Could not add friends in profile service for match {}: {}", matchId, e.getMessage());
                throw new ExternalServiceException("Cannot accept match right now, please try again later");
            }
        }

        Match saved = matchRepository.save(match);
        matchEventPublisher.publishMatchResponse(match.getRequesterId(), match.getTargetId(), saved.getStatus());
        return saved;
    }

    @Override
    public void cancelMatch(UUID matchId, UUID requesterId) {
        Match match = getMatch(matchId);
        if (!match.getRequesterId().equals(requesterId)) {
            throw new InvalidInputException("Only the sender of a match request can cancel it");
        }
        if (match.getStatus() != MatchStatus.PENDING) {
            throw new InvalidInputException("Only pending match requests can be cancelled");
        }
        matchRepository.delete(matchId);
    }

    @Override
    public Relationship getRelationship(UUID userId, UUID otherUserId) {
        if (profileServicePort.getFriends(userId).contains(otherUserId)) {
            return Relationship.of(Relationship.RelationshipStatus.FRIEND, null);
        }

        Optional<Match> sent = matchRepository.findByRequesterId(userId).stream()
                .filter(m -> m.getTargetId().equals(otherUserId) && m.getStatus() == MatchStatus.PENDING)
                .findFirst();
        if (sent.isPresent()) {
            return Relationship.of(Relationship.RelationshipStatus.PENDING_SENT, sent.get().getIdMatch());
        }

        Optional<Match> received = matchRepository.findByTargetId(userId).stream()
                .filter(m -> m.getRequesterId().equals(otherUserId) && m.getStatus() == MatchStatus.PENDING)
                .findFirst();
        if (received.isPresent()) {
            return Relationship.of(Relationship.RelationshipStatus.PENDING_RECEIVED, received.get().getIdMatch());
        }

        return Relationship.of(Relationship.RelationshipStatus.NONE, null);
    }

    @Override
    public void removeFriend(UUID userId, UUID friendId) {
        try {
            profileServicePort.removeFriend(userId, friendId);
        } catch (Exception e) {
            log.warn("Could not remove friendship in profile service for {}/{}: {}", userId, friendId, e.getMessage());
            throw new ExternalServiceException("Cannot remove friend right now, please try again later");
        }

        // Limpia cualquier match ACCEPTED que quede entre los dos (en cualquier
        // dirección) para que puedan volver a conectar más adelante sin chocar
        // con el existsByRequesterIdAndTargetId/findExistingBetween de arriba.
        findExistingBetween(userId, friendId)
                .filter(m -> m.getStatus() == MatchStatus.ACCEPTED)
                .ifPresent(m -> matchRepository.delete(m.getIdMatch()));
    }

    private Optional<Match> findExistingBetween(UUID a, UUID b) {
        return matchRepository.findByRequesterId(a).stream()
                .filter(m -> m.getTargetId().equals(b))
                .findFirst()
                .or(() -> matchRepository.findByRequesterId(b).stream()
                        .filter(m -> m.getTargetId().equals(a))
                        .findFirst());
    }
}
