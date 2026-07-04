package co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.escuelaing.alphaeci.matching_service.domain.model.Match;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.enums.MatchStatus;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.entity.MatchDocument;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.mapper.MatchPersistenceMapper;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.repository.MatchMongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchRepositoryAdapterTest {

    @Mock
    private MatchMongoRepository mongoRepository;

    @Mock
    private MatchPersistenceMapper mapper;

    @InjectMocks
    private MatchRepositoryAdapter adapter;

    private UUID matchId;
    private UUID requesterId;
    private UUID targetId;
    private Match match;
    private MatchDocument doc;

    @BeforeEach
    void setUp() {
        matchId = UUID.randomUUID();
        requesterId = UUID.randomUUID();
        targetId = UUID.randomUUID();

        match = new Match();
        match.setIdMatch(matchId);
        match.setRequesterId(requesterId);
        match.setTargetId(targetId);
        match.setStatus(MatchStatus.PENDING);
        match.setCreatedAt(LocalDateTime.now());
        match.setUpdatedAt(LocalDateTime.now());

        doc = new MatchDocument(matchId, requesterId, targetId, MatchStatus.PENDING, null,
                LocalDateTime.now(), LocalDateTime.now());
    }

    // ======================== save ========================

    @Test
    void save_returnsMappedDomain() {
        when(mapper.toDocument(match)).thenReturn(doc);
        when(mongoRepository.save(doc)).thenReturn(doc);
        when(mapper.toDomain(doc)).thenReturn(match);

        Match result = adapter.save(match);

        assertThat(result).isEqualTo(match);
        verify(mongoRepository).save(doc);
    }

    // ======================== findById ========================

    @Test
    void findById_found_returnsMappedMatch() {
        when(mongoRepository.findById(matchId)).thenReturn(Optional.of(doc));
        when(mapper.toDomain(doc)).thenReturn(match);

        Optional<Match> result = adapter.findById(matchId);

        assertThat(result).isPresent().contains(match);
    }

    @Test
    void findById_notFound_returnsEmpty() {
        when(mongoRepository.findById(matchId)).thenReturn(Optional.empty());

        Optional<Match> result = adapter.findById(matchId);

        assertThat(result).isEmpty();
    }

    // ======================== existsByRequesterIdAndTargetId ========================

    @Test
    void existsByRequesterIdAndTargetId_returnsTrue() {
        when(mongoRepository.existsByRequesterIdAndTargetId(requesterId, targetId)).thenReturn(true);

        assertThat(adapter.existsByRequesterIdAndTargetId(requesterId, targetId)).isTrue();
    }

    @Test
    void existsByRequesterIdAndTargetId_returnsFalse() {
        when(mongoRepository.existsByRequesterIdAndTargetId(requesterId, targetId)).thenReturn(false);

        assertThat(adapter.existsByRequesterIdAndTargetId(requesterId, targetId)).isFalse();
    }

    // ======================== findByTargetId ========================

    @Test
    void findByTargetId_returnsMappedList() {
        when(mongoRepository.findByTargetId(targetId)).thenReturn(List.of(doc));
        when(mapper.toDomain(doc)).thenReturn(match);

        List<Match> result = adapter.findByTargetId(targetId);

        assertThat(result).containsExactly(match);
    }

    @Test
    void findByTargetId_emptyList() {
        when(mongoRepository.findByTargetId(targetId)).thenReturn(List.of());

        List<Match> result = adapter.findByTargetId(targetId);

        assertThat(result).isEmpty();
    }

    // ======================== findByRequesterId ========================

    @Test
    void findByRequesterId_returnsMappedList() {
        when(mongoRepository.findByRequesterId(requesterId)).thenReturn(List.of(doc));
        when(mapper.toDomain(doc)).thenReturn(match);

        List<Match> result = adapter.findByRequesterId(requesterId);

        assertThat(result).containsExactly(match);
    }

    @Test
    void findByRequesterId_emptyList() {
        when(mongoRepository.findByRequesterId(requesterId)).thenReturn(List.of());

        List<Match> result = adapter.findByRequesterId(requesterId);

        assertThat(result).isEmpty();
    }

    // ======================== delete ========================

    @Test
    void delete_callsRepositoryDeleteById() {
        doNothing().when(mongoRepository).deleteById(matchId);

        adapter.delete(matchId);

        verify(mongoRepository).deleteById(matchId);
    }
}
