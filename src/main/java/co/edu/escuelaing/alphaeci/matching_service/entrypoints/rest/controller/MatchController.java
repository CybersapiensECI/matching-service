package co.edu.escuelaing.alphaeci.matching_service.entrypoints.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.escuelaing.alphaeci.matching_service.application.dto.request.FilterCriteriaRequest;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.request.MatchRequest;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.request.MatchUpdateRequest;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.response.MatchResponse;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.response.RelationshipResponse;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.response.NearbyRecommendationResponse;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.response.RecommendationResponse;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.response.RecommendationWithScoreResponse;
import co.edu.escuelaing.alphaeci.matching_service.application.mapper.MatchApplicationMapper;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.Match;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.enums.MatchStatus;
import co.edu.escuelaing.alphaeci.matching_service.domain.ports.in.MatchUseCasePort;
import co.edu.escuelaing.alphaeci.matching_service.domain.ports.in.RecommendationsUseCasePort;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.AffinityScore;
import co.edu.escuelaing.alphaeci.matching_service.entrypoints.advice.ErrorResponse;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/matches")
@Tag(name = "Matches - Management", description = "Create, respond to and cancel match requests. Also contains recommendation retrieval endpoints.")
@RequiredArgsConstructor
public class MatchController {

    private final MatchUseCasePort matchUseCase;
    private final RecommendationsUseCasePort recommendationsUseCase;
    private final MatchApplicationMapper matchRestMapper;

    // ---------------- CREATE MATCH ----------------
    @PostMapping
    @Operation(summary = "Create match request",
               description = "Sends a match request from one user to another. Creates the Match resource with status PENDING. Returns the created MatchResponse.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Match request created",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchResponse.class),
                examples = @ExampleObject(value = "{\"idMatch\":\"00000000-0000-0000-0000-000000000000\",\"requesterId\":\"11111111-1111-1111-1111-111111111111\",\"targetId\":\"22222222-2222-2222-2222-222222222222\",\"status\":\"PENDING\"}"))),
        @ApiResponse(responseCode = "400", description = "Bad request: invalid payload or match already exists",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Match already exists between these users.\",\"status\":400,\"timestamp\":\"2026-05-22T12:00:00\"}")))
    })
    public ResponseEntity<MatchResponse> createMatch(@Valid @RequestBody MatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                matchRestMapper.toResponse(matchUseCase.createMatch(request.getRequesterId(), request.getTargetId())));
    }

    // ---------------- GET MATCH BY ID ----------------
    @GetMapping("/{id}")
    @Operation(summary = "Get match by ID",
               description = "Retrieves a match resource by its UUID. Useful to inspect current status and affinity details.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Match retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchResponse.class),
                examples = @ExampleObject(value = "{\"idMatch\":\"00000000-0000-0000-0000-000000000000\",\"requesterId\":\"11111111-1111-1111-1111-111111111111\",\"targetId\":\"22222222-2222-2222-2222-222222222222\",\"status\":\"PENDING\"}"))),
        @ApiResponse(responseCode = "404", description = "Not found: match does not exist",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Match not found.\",\"status\":404,\"timestamp\":\"2026-05-22T12:00:00\"}")))
    })
    public ResponseEntity<MatchResponse> getMatch(
            @Parameter(description = "ID of the match") @PathVariable UUID id) {
        return ResponseEntity.ok(matchRestMapper.toResponse(matchUseCase.getMatch(id)));
    }

    // ---------------- GET MATCHES POR USUARIO ----------------
    @GetMapping("/user/{userId}")
    @Operation(summary = "List all matches for a user",
               description = "Returns matches where the given user is either the requester or the target. Useful for inbox or history views.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Matches retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchResponse.class),
                examples = @ExampleObject(value = "[{\"idMatch\":\"00000000-0000-0000-0000-000000000000\",\"requesterId\":\"1111...\",\"targetId\":\"2222...\",\"status\":\"PENDING\"}]"))),
        @ApiResponse(responseCode = "400", description = "Bad request: invalid userId format",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Invalid UUID format.\",\"status\":400,\"timestamp\":\"2026-05-22T12:00:00\"}")))
    })
    public ResponseEntity<List<MatchResponse>> getMatchesByUser(
            @Parameter(description = "ID of the user") @PathVariable UUID userId) {
        List<Match> requesterMatches = matchUseCase.findByRequesterId(userId);
        List<Match> targetMatches = matchUseCase.findByTargetId(userId);

        List<MatchResponse> matches = matchRestMapper.toResponseList(requesterMatches);
        matches.addAll(matchRestMapper.toResponseList(targetMatches));

        return ResponseEntity.ok(matches);
    }

    @GetMapping("/user/{userId}/sent")
    @Operation(summary = "List sent match requests",
               description = "Returns match requests initiated by the given user.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sent matches retrieved",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchResponse.class),
                examples = @ExampleObject(value = "[{\"idMatch\":\"...\",\"requesterId\":\"1111...\",\"targetId\":\"2222...\",\"status\":\"PENDING\"}]"))),
        @ApiResponse(responseCode = "400", description = "Bad request: invalid userId format",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Invalid UUID format.\",\"status\":400,\"timestamp\":\"2026-05-22T12:00:00\"}")))
    })
    public ResponseEntity<List<MatchResponse>> getSentMatchesByUser(
            @Parameter(description = "ID of the user") @PathVariable UUID userId) {
        return ResponseEntity.ok(matchRestMapper.toResponseList(matchUseCase.findByRequesterId(userId)));
    }

    @GetMapping("/user/{userId}/received")
    @Operation(summary = "List received match requests",
               description = "Returns match requests where the given user is the recipient.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Received matches retrieved",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchResponse.class),
                examples = @ExampleObject(value = "[{\"idMatch\":\"...\",\"requesterId\":\"1111...\",\"targetId\":\"2222...\",\"status\":\"PENDING\"}]"))),
        @ApiResponse(responseCode = "400", description = "Bad request: invalid userId format",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Invalid UUID format.\",\"status\":400,\"timestamp\":\"2026-05-22T12:00:00\"}")))
    })
    public ResponseEntity<List<MatchResponse>> getReceivedMatchesByUser(
            @Parameter(description = "ID of the user") @PathVariable UUID userId) {
        return ResponseEntity.ok(matchRestMapper.toResponseList(matchUseCase.findByTargetId(userId)));
    }

    // ---------------- UPDATE STATUS ----------------
    @PatchMapping("/{id}/status")
    @Operation(summary = "Respond to match request (accept/reject)",
               description = "Allows the recipient to accept or reject a pending match request. Requires the responder's userId as request parameter.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchResponse.class),
                examples = @ExampleObject(value = "{\"idMatch\":\"00000000-0000-0000-0000-000000000000\",\"status\":\"ACCEPTED\"}"))),
        @ApiResponse(responseCode = "400", description = "Bad request: invalid status or unauthorized responder",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Invalid operation or user is not the recipient.\",\"status\":400,\"timestamp\":\"2026-05-22T12:00:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Not found: match does not exist",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Match not found.\",\"status\":404,\"timestamp\":\"2026-05-22T12:00:00\"}")))
    })
    public ResponseEntity<MatchResponse> updateMatchStatus(
            @Parameter(description = "ID of the match") @PathVariable UUID id,
            @Parameter(description = "ID of the user responding") @RequestParam UUID userId,
            @Valid @RequestBody MatchUpdateRequest request) {
        boolean accept = request.getStatus() == MatchStatus.ACCEPTED;
        return ResponseEntity.ok(matchRestMapper.toResponse(matchUseCase.respondToMatchRequest(id, userId, accept)));
    }

    // ---------------- CANCEL MATCH ----------------
    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel match request",
               description = "Cancels a pending match request. Only the original requester may cancel.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Match cancelled successfully"),
        @ApiResponse(responseCode = "404", description = "Not found: match does not exist",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Match not found.\",\"status\":404,\"timestamp\":\"2026-05-22T12:00:00\"}"))),
        @ApiResponse(responseCode = "400", description = "Bad request: user not authorized or match not pending",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Cannot cancel: not the sender or status is not pending.\",\"status\":400,\"timestamp\":\"2026-05-22T12:00:00\"}")))
    })
    public ResponseEntity<Void> cancelMatch(
            @Parameter(description = "ID of the match") @PathVariable UUID id,
            @Parameter(description = "ID of the user cancelling") @RequestParam UUID userId) {
        matchUseCase.cancelMatch(id, userId);
        return ResponseEntity.noContent().build();
    }

    // ---------------- RELATIONSHIP STATUS ----------------
    @GetMapping("/relationship")
    @Operation(summary = "Get relationship status between two users",
               description = "Single source of truth for a public profile's connect/message button: FRIEND, "
                       + "PENDING_SENT, PENDING_RECEIVED or NONE. Checks profile-service's friendsId first, "
                       + "then pending match requests in both directions.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Relationship retrieved",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RelationshipResponse.class),
                examples = @ExampleObject(value = "{\"status\":\"FRIEND\",\"matchId\":null}")))
    })
    public ResponseEntity<RelationshipResponse> getRelationship(
            @Parameter(description = "ID of the user asking") @RequestParam UUID userId,
            @Parameter(description = "ID of the other user") @RequestParam UUID otherUserId) {
        return ResponseEntity.ok(RelationshipResponse.from(matchUseCase.getRelationship(userId, otherUserId)));
    }

    // ---------------- REMOVE FRIEND ----------------
    @DeleteMapping("/friends/{friendId}")
    @Operation(summary = "Remove a friendship",
               description = "Unfriends both users in profile-service and clears the underlying ACCEPTED match "
                       + "so they can send a new match request later if they want to reconnect.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Friendship removed successfully")
    })
    public ResponseEntity<Void> removeFriend(
            @Parameter(description = "ID of the user removing the friend") @RequestParam UUID userId,
            @Parameter(description = "ID of the friend to remove") @PathVariable UUID friendId) {
        matchUseCase.removeFriend(userId, friendId);
        return ResponseEntity.noContent().build();
    }

    // ---------------- GET RECOMMENDATIONS ----------------
    @GetMapping("/recommendations/{userId}")
    @Operation(summary = "Get recommendations for user",
               description = "Returns a set of recommended user ids for the requester based on affinity calculations. Useful to show suggested profiles.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Recommendations retrieved",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RecommendationResponse.class),
                examples = @ExampleObject(value = "{\"userId\":\"11111111-1111-1111-1111-111111111111\",\"recommendedIds\":[\"22222222-2222-2222-2222-222222222222\"]}"))),
        @ApiResponse(responseCode = "400", description = "Bad request: invalid userId",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Invalid UUID format.\",\"status\":400,\"timestamp\":\"2026-05-22T12:00:00\"}")))
    })
    public ResponseEntity<RecommendationResponse> getRecommendations(@PathVariable UUID userId) {
        return ResponseEntity.ok(matchRestMapper.toRecommendationResponse(userId, recommendationsUseCase.getRecommendedUserIdsForUser(userId)));
    }

    @GetMapping("/recommendations/{userId}/nearby")
    @Operation(
        summary = "Get nearby recommendations",
        description = "Returns recommended users constrained by geolocation, including their distance from the requester. Useful for showing local suggestions."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Nearby recommendations retrieved",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = NearbyRecommendationResponse.class),
                examples = @ExampleObject(value = "[{\"userId\":\"22222222-2222-2222-2222-222222222222\",\"distanceMeters\":1200}]"))),
        @ApiResponse(responseCode = "400", description = "Bad request: invalid userId or location data",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Invalid request.\",\"status\":400,\"timestamp\":\"2026-05-22T12:00:00\"}")))
    })
    public ResponseEntity<List<NearbyRecommendationResponse>> getNearbyRecommendations(@PathVariable UUID userId) {
        List<NearbyRecommendationResponse> response = recommendationsUseCase.getNearbyRecommendationsForUser(userId).stream()
                .map(matchRestMapper::toNearbyRecommendationResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recommendations/{userId}/scores")
    @Operation(
        summary = "Get recommendations with affinity scores",
        description = "Returns recommended users with detailed affinity score breakdown (interest, academic, schedule) sorted by total score. Useful for debugging and ranking explanations."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Recommendations with scores retrieved",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RecommendationWithScoreResponse.class),
                examples = @ExampleObject(value = "[{\"userId\":\"22222222-2222-2222-2222-222222222222\",\"score\":0.85,\"interestScore\":0.5,\"academicScore\":0.2,\"scheduleScore\":0.15}]"))),
        @ApiResponse(responseCode = "400", description = "Bad request: invalid userId",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Invalid UUID format.\",\"status\":400,\"timestamp\":\"2026-05-22T12:00:00\"}")))
    })
    public ResponseEntity<List<RecommendationWithScoreResponse>> getRecommendationsWithScores(@PathVariable UUID userId) {
        Map<UUID, AffinityScore> scores = recommendationsUseCase.getRecommendationsForUser(userId);
        List<RecommendationWithScoreResponse> response = scores.entrySet().stream()
                .sorted(Map.Entry.<UUID, AffinityScore>comparingByValue(
                        Comparator.comparingDouble(AffinityScore::getTotalScore)).reversed())
                .map(e -> matchRestMapper.toRecommendationWithScoreResponse(e.getKey(), e.getValue()))
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recommendations/{userId}/filtered")
    @Operation(
        summary = "Get filtered recommendations",
        description = "Returns recommended user IDs filtered by career, semester, tag, active status and/or geolocation proximity"
    )
    @ApiResponse(responseCode = "200", description = "List of filtered recommended user IDs")
    public ResponseEntity<RecommendationResponse> getFilteredRecommendations(
            @PathVariable UUID userId,
            @RequestBody FilterCriteriaRequest filters) {
        List<UUID> ids = recommendationsUseCase.getFilteredRecommendations(userId, filters);
        return ResponseEntity.ok(matchRestMapper.toRecommendationResponse(userId, ids));
    }
}