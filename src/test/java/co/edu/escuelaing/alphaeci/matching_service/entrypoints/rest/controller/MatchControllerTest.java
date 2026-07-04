package co.edu.escuelaing.alphaeci.matching_service.entrypoints.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import co.edu.escuelaing.alphaeci.matching_service.application.dto.request.MatchRequest;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.request.MatchUpdateRequest;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.response.AffinityScoreResponse;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.response.MatchResponse;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.response.NearbyRecommendationResponse;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.response.RecommendationResponse;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.response.RecommendationWithScoreResponse;
import co.edu.escuelaing.alphaeci.matching_service.application.mapper.MatchApplicationMapper;
import co.edu.escuelaing.alphaeci.matching_service.domain.exceptions.InvalidInputException;
import co.edu.escuelaing.alphaeci.matching_service.domain.exceptions.NotFoundException;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.Match;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.NearbyRecommendation;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.NearbyUserDistance;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.enums.MatchStatus;
import co.edu.escuelaing.alphaeci.matching_service.domain.ports.in.MatchUseCasePort;
import co.edu.escuelaing.alphaeci.matching_service.domain.ports.in.RecommendationsUseCasePort;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.AffinityScore;
import co.edu.escuelaing.alphaeci.matching_service.entrypoints.advice.GlobalExceptionHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MatchControllerTest {

    @Mock
    private MatchUseCasePort matchUseCase;

    @Mock
    private RecommendationsUseCasePort recommendationsUseCase;

    @Mock
    private MatchApplicationMapper matchRestMapper;

    @InjectMocks
    private MatchController matchController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UUID requesterId;
    private UUID targetId;
    private UUID matchId;
    private Match match;
    private MatchResponse matchResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(matchController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        requesterId = UUID.randomUUID();
        targetId = UUID.randomUUID();
        matchId = UUID.randomUUID();

        match = new Match();
        match.setIdMatch(matchId);
        match.setRequesterId(requesterId);
        match.setTargetId(targetId);
        match.setStatus(MatchStatus.PENDING);
        match.setAffinityScore(new AffinityScore());
        match.setCreatedAt(LocalDateTime.now());
        match.setUpdatedAt(LocalDateTime.now());

        matchResponse = MatchResponse.builder()
                .idMatch(matchId)
                .requesterId(requesterId)
                .targetId(targetId)
                .status(MatchStatus.PENDING)
                .affinityScore(AffinityScoreResponse.builder().build())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ======================== CREATE MATCH ========================

    @Test
    void createMatch_success_returns201() throws Exception {
        MatchRequest request = new MatchRequest();
        request.setRequesterId(requesterId);
        request.setTargetId(targetId);

        when(matchUseCase.createMatch(requesterId, targetId)).thenReturn(match);
        when(matchRestMapper.toResponse(match)).thenReturn(matchResponse);

        mockMvc.perform(post("/api/v1/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMatch").value(matchId.toString()));
    }

    @Test
    void createMatch_nullRequesterId_returns400() throws Exception {
        MatchRequest request = new MatchRequest();
        request.setRequesterId(null);
        request.setTargetId(targetId);

        mockMvc.perform(post("/api/v1/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMatch_nullTargetId_returns400() throws Exception {
        MatchRequest request = new MatchRequest();
        request.setRequesterId(requesterId);
        request.setTargetId(null);

        mockMvc.perform(post("/api/v1/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMatch_alreadyExists_returns400() throws Exception {
        MatchRequest request = new MatchRequest();
        request.setRequesterId(requesterId);
        request.setTargetId(targetId);

        when(matchUseCase.createMatch(requesterId, targetId))
                .thenThrow(new InvalidInputException("Already exists"));

        mockMvc.perform(post("/api/v1/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMatch_targetIsFriend_returns400() throws Exception {
        MatchRequest request = new MatchRequest();
        request.setRequesterId(requesterId);
        request.setTargetId(targetId);

        when(matchUseCase.createMatch(requesterId, targetId))
                .thenThrow(new InvalidInputException("already your friend"));

        mockMvc.perform(post("/api/v1/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ======================== GET MATCH ========================

    @Test
    void getMatch_success_returns200() throws Exception {
        when(matchUseCase.getMatch(matchId)).thenReturn(match);
        when(matchRestMapper.toResponse(match)).thenReturn(matchResponse);

        mockMvc.perform(get("/api/v1/matches/{id}", matchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMatch").value(matchId.toString()));
    }

    @Test
    void getMatch_notFound_returns404() throws Exception {
        when(matchUseCase.getMatch(matchId)).thenThrow(new NotFoundException("Match not found"));

        mockMvc.perform(get("/api/v1/matches/{id}", matchId))
                .andExpect(status().isNotFound());
    }

    // ======================== GET MATCHES BY USER ========================

    @Test
    void getMatchesByUser_returns200() throws Exception {
        when(matchUseCase.findByRequesterId(requesterId)).thenReturn(List.of(match));
        when(matchUseCase.findByTargetId(requesterId)).thenReturn(List.of());
        when(matchRestMapper.toResponseList(List.of(match)))
                .thenReturn(new java.util.ArrayList<>(List.of(matchResponse)));
        when(matchRestMapper.toResponseList(List.of()))
                .thenReturn(new java.util.ArrayList<>());

        mockMvc.perform(get("/api/v1/matches/user/{userId}", requesterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idMatch").value(matchId.toString()));
    }

    // ======================== UPDATE STATUS ========================

    @Test
    void updateMatchStatus_accept_returns200() throws Exception {
        MatchUpdateRequest request = new MatchUpdateRequest();
        request.setStatus(MatchStatus.ACCEPTED);

        MatchResponse acceptedResponse = MatchResponse.builder()
                .idMatch(matchId).status(MatchStatus.ACCEPTED)
                .affinityScore(AffinityScoreResponse.builder().build())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(matchUseCase.respondToMatchRequest(matchId, targetId, true)).thenReturn(match);
        when(matchRestMapper.toResponse(match)).thenReturn(acceptedResponse);

        mockMvc.perform(patch("/api/v1/matches/{id}/status", matchId)
                        .param("userId", targetId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void updateMatchStatus_reject_returns200() throws Exception {
        MatchUpdateRequest request = new MatchUpdateRequest();
        request.setStatus(MatchStatus.REJECTED);

        MatchResponse rejectedResponse = MatchResponse.builder()
                .idMatch(matchId).status(MatchStatus.REJECTED)
                .affinityScore(AffinityScoreResponse.builder().build())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(matchUseCase.respondToMatchRequest(matchId, targetId, false)).thenReturn(match);
        when(matchRestMapper.toResponse(match)).thenReturn(rejectedResponse);

        mockMvc.perform(patch("/api/v1/matches/{id}/status", matchId)
                        .param("userId", targetId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void updateMatchStatus_nullStatus_returns400() throws Exception {
        MatchUpdateRequest request = new MatchUpdateRequest();
        request.setStatus(null);

        mockMvc.perform(patch("/api/v1/matches/{id}/status", matchId)
                        .param("userId", targetId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMatchStatus_missingUserId_returns400() throws Exception {
        MatchUpdateRequest request = new MatchUpdateRequest();
        request.setStatus(MatchStatus.ACCEPTED);

        mockMvc.perform(patch("/api/v1/matches/{id}/status", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMatchStatus_notFound_returns404() throws Exception {
        MatchUpdateRequest request = new MatchUpdateRequest();
        request.setStatus(MatchStatus.ACCEPTED);

        when(matchUseCase.respondToMatchRequest(matchId, targetId, true))
                .thenThrow(new NotFoundException("Match not found"));

        mockMvc.perform(patch("/api/v1/matches/{id}/status", matchId)
                        .param("userId", targetId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateMatchStatus_wrongResponder_returns400() throws Exception {
        MatchUpdateRequest request = new MatchUpdateRequest();
        request.setStatus(MatchStatus.ACCEPTED);
        UUID wrongUser = UUID.randomUUID();

        when(matchUseCase.respondToMatchRequest(matchId, wrongUser, true))
                .thenThrow(new InvalidInputException("Only the recipient"));

        mockMvc.perform(patch("/api/v1/matches/{id}/status", matchId)
                        .param("userId", wrongUser.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ======================== CANCEL MATCH ========================

    @Test
    void cancelMatch_success_returns204() throws Exception {
        doNothing().when(matchUseCase).cancelMatch(matchId, requesterId);

        mockMvc.perform(delete("/api/v1/matches/{id}", matchId)
                        .param("userId", requesterId.toString()))
                .andExpect(status().isNoContent());

        verify(matchUseCase).cancelMatch(matchId, requesterId);
    }

    @Test
    void cancelMatch_notFound_returns404() throws Exception {
        doThrow(new NotFoundException("Match not found")).when(matchUseCase).cancelMatch(matchId, requesterId);

        mockMvc.perform(delete("/api/v1/matches/{id}", matchId)
                        .param("userId", requesterId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelMatch_wrongUser_returns400() throws Exception {
        UUID wrongUser = UUID.randomUUID();
        doThrow(new InvalidInputException("Only the sender")).when(matchUseCase).cancelMatch(matchId, wrongUser);

        mockMvc.perform(delete("/api/v1/matches/{id}", matchId)
                        .param("userId", wrongUser.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelMatch_missingUserId_returns400() throws Exception {
        mockMvc.perform(delete("/api/v1/matches/{id}", matchId))
                .andExpect(status().isBadRequest());
    }

    // ======================== RECOMMENDATIONS ========================

    @Test
    void getRecommendations_returns200() throws Exception {
        RecommendationResponse recommendation = RecommendationResponse.builder()
                .userId(requesterId)
                .recommendedUserIds(List.of(targetId))
                .build();

        when(recommendationsUseCase.getRecommendedUserIdsForUser(requesterId)).thenReturn(List.of(targetId));
        when(matchRestMapper.toRecommendationResponse(requesterId, List.of(targetId))).thenReturn(recommendation);

        mockMvc.perform(get("/api/v1/matches/recommendations/{userId}", requesterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(requesterId.toString()));
    }

    @Test
    void getRecommendationsWithScores_returns200() throws Exception {
        AffinityScore score = new AffinityScore();
        score.setTotalScore(0.85);
        score.setInterestScore(0.9);
        score.setAcademicScore(0.8);
        score.setScheduleScore(0.75);

        RecommendationWithScoreResponse scoreResponse = RecommendationWithScoreResponse.builder()
                .targetUserId(targetId)
                .totalScore(0.85)
                .interestScore(0.9)
                .academicScore(0.8)
                .scheduleScore(0.75)
                .build();

        when(recommendationsUseCase.getRecommendationsForUser(requesterId)).thenReturn(Map.of(targetId, score));
        when(matchRestMapper.toRecommendationWithScoreResponse(targetId, score)).thenReturn(scoreResponse);

        mockMvc.perform(get("/api/v1/matches/recommendations/{userId}/scores", requesterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].targetUserId").value(targetId.toString()))
                .andExpect(jsonPath("$[0].totalScore").value(0.85));
    }

    @Test
    void getNearbyRecommendations_returns200() throws Exception {
        AffinityScore score = new AffinityScore();
        score.setTotalScore(0.92);
        score.setInterestScore(0.9);
        score.setAcademicScore(0.85);
        score.setScheduleScore(0.8);

        NearbyRecommendation nearbyRecommendation = new NearbyRecommendation(new NearbyUserDistance(targetId, 150.0), score);

        NearbyRecommendationResponse nearbyResponse = NearbyRecommendationResponse.builder()
                .targetUserId(targetId)
                .distanceMeters(150.0)
                .totalScore(0.92)
                .interestScore(0.9)
                .academicScore(0.85)
                .scheduleScore(0.8)
                .build();

        when(recommendationsUseCase.getNearbyRecommendationsForUser(requesterId)).thenReturn(List.of(nearbyRecommendation));
        when(matchRestMapper.toNearbyRecommendationResponse(nearbyRecommendation)).thenReturn(nearbyResponse);

        mockMvc.perform(get("/api/v1/matches/recommendations/{userId}/nearby", requesterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].targetUserId").value(targetId.toString()))
                .andExpect(jsonPath("$[0].distanceMeters").value(150.0))
                .andExpect(jsonPath("$[0].totalScore").value(0.92));
    }
}
