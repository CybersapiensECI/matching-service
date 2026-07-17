package co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.profile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import co.edu.escuelaing.alphaeci.matching_service.domain.exceptions.ExternalServiceException;
import co.edu.escuelaing.alphaeci.matching_service.domain.exceptions.NotFoundException;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.MatchProfile;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.profile.client.ProfileFeignClient;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.profile.client.ProfilePublicFeignClient;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.profile.dto.UserMatchProfileDto;
import feign.Request;
import feign.Response;
import feign.FeignException;

public class ProfileServiceAdapterTest {

    @Mock
    ProfileFeignClient profileFeignClient;

    @Mock
    ProfilePublicFeignClient profilePublicFeignClient;

    ProfileServiceAdapter adapter;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        adapter = new ProfileServiceAdapter(profileFeignClient, profilePublicFeignClient);
    }

    @Test
    void getProfileById_success_mapsDto() {
        UUID id = UUID.randomUUID();
        UserMatchProfileDto dto = new UserMatchProfileDto();
        dto.setId(id.toString());
        dto.setCareer("COMPUTER_SCIENCE");
        dto.setSemester(3);
        dto.setTags(List.of("t1","t2"));
        dto.setSchedulesAvailable(List.of("MON_8"));
        dto.setActive(true);

        when(profileFeignClient.getProfileById(id)).thenReturn(dto);

        MatchProfile profile = adapter.getProfileById(id);

        assertEquals(id, profile.getId());
        assertEquals("COMPUTER_SCIENCE", profile.getCareer());
        assertEquals(3, profile.getSemester());
        assertTrue(profile.isActive());
    }

    @Test
    void getProfileById_notFound_throwsNotFoundException() {
        UUID id = UUID.randomUUID();

        Request req = Request.create(Request.HttpMethod.GET, "http://x", java.util.Collections.emptyMap(), null, Charset.defaultCharset(), null);
        Response resp = Response.builder().status(404).reason("Not Found").request(req).build();
        FeignException fe = FeignException.errorStatus("getProfileById", resp);

        when(profileFeignClient.getProfileById(id)).thenThrow(fe);

        assertThrows(NotFoundException.class, () -> adapter.getProfileById(id));
    }

    @Test
    void getAllProfiles_whenFeignFails_throwsExternalServiceException() {
        when(profileFeignClient.getAllProfiles()).thenThrow(FeignException.errorStatus("getAll", Response.builder().status(500).reason("err").request(Request.create(Request.HttpMethod.GET, "http://x", java.util.Collections.emptyMap(), null, Charset.defaultCharset(), null)).build()));

        assertThrows(ExternalServiceException.class, () -> adapter.getAllProfiles());
    }

    @Test
    void getFriends_success_and_addFriend_invokesClient() {
        UUID id = UUID.randomUUID();
        when(profilePublicFeignClient.getFriends(id)).thenReturn(List.of(UUID.randomUUID()));

        var friends = adapter.getFriends(id);
        assertFalse(friends.isEmpty());

        UUID friendId = UUID.randomUUID();
        adapter.addFriend(id, friendId);
        verify(profilePublicFeignClient).addFriend(eq(id), any());
    }

    private static FeignException serverError(String methodKey) {
        Request req = Request.create(Request.HttpMethod.GET, "http://x", java.util.Collections.emptyMap(), null, Charset.defaultCharset(), null);
        Response resp = Response.builder().status(500).reason("err").request(req).build();
        return FeignException.errorStatus(methodKey, resp);
    }

    @Test
    void getProfileById_genericFeignException_throwsExternalServiceException() {
        UUID id = UUID.randomUUID();
        when(profileFeignClient.getProfileById(id)).thenThrow(serverError("getProfileById"));

        assertThrows(ExternalServiceException.class, () -> adapter.getProfileById(id));
    }

    @Test
    void getAllProfiles_success_mapsListOfDtos() {
        UserMatchProfileDto dto = new UserMatchProfileDto();
        dto.setId(UUID.randomUUID().toString());
        dto.setCareer("MATHEMATICS");
        when(profileFeignClient.getAllProfiles()).thenReturn(List.of(dto));

        var result = adapter.getAllProfiles();

        assertEquals(1, result.size());
        assertEquals("MATHEMATICS", result.get(0).getCareer());
    }

    @Test
    void getAllProfiles_nonUuidId_isSkippedNotThrown() {
        UserMatchProfileDto valid = new UserMatchProfileDto();
        valid.setId(UUID.randomUUID().toString());
        valid.setCareer("MATHEMATICS");
        UserMatchProfileDto malformed = new UserMatchProfileDto();
        malformed.setId("5e46381adc9e7f1319f50f926ec71bb2");
        malformed.setCareer("SEED_DATA");
        when(profileFeignClient.getAllProfiles()).thenReturn(List.of(valid, malformed));

        var result = adapter.getAllProfiles();

        assertEquals(1, result.size());
        assertEquals("MATHEMATICS", result.get(0).getCareer());
    }

    @Test
    void getAllProfilesExcludingUser_success_mapsListOfDtos() {
        UUID excludeId = UUID.randomUUID();
        UserMatchProfileDto dto = new UserMatchProfileDto();
        dto.setId(UUID.randomUUID().toString());
        when(profileFeignClient.getAllProfiles(excludeId)).thenReturn(List.of(dto));

        var result = adapter.getAllProfiles(excludeId);

        assertEquals(1, result.size());
    }

    @Test
    void getAllProfilesExcludingUser_feignException_throwsExternalServiceException() {
        UUID excludeId = UUID.randomUUID();
        when(profileFeignClient.getAllProfiles(excludeId)).thenThrow(serverError("getAllProfilesCandidates"));

        assertThrows(ExternalServiceException.class, () -> adapter.getAllProfiles(excludeId));
    }

    @Test
    void getFriends_feignException_throwsExternalServiceException() {
        UUID id = UUID.randomUUID();
        when(profilePublicFeignClient.getFriends(id)).thenThrow(serverError("getFriends"));

        assertThrows(ExternalServiceException.class, () -> adapter.getFriends(id));
    }

    @Test
    void addFriend_feignException_throwsExternalServiceException() {
        UUID id = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();
        doThrow(serverError("addFriend")).when(profilePublicFeignClient).addFriend(eq(id), any());

        assertThrows(ExternalServiceException.class, () -> adapter.addFriend(id, friendId));
    }

    @Test
    void isGeolocationEnabled_true_returnsTrue() {
        UUID id = UUID.randomUUID();
        when(profileFeignClient.isGeolocationEnabled(id)).thenReturn(true);

        assertTrue(adapter.isGeolocationEnabled(id));
    }

    @Test
    void isGeolocationEnabled_null_returnsFalse() {
        UUID id = UUID.randomUUID();
        when(profileFeignClient.isGeolocationEnabled(id)).thenReturn(null);

        assertFalse(adapter.isGeolocationEnabled(id));
    }

    @Test
    void isGeolocationEnabled_feignException_throwsExternalServiceException() {
        UUID id = UUID.randomUUID();
        when(profileFeignClient.isGeolocationEnabled(id)).thenThrow(serverError("isGeolocationEnabled"));

        assertThrows(ExternalServiceException.class, () -> adapter.isGeolocationEnabled(id));
    }

    @Test
    void isActive_true_returnsTrue() {
        UUID id = UUID.randomUUID();
        when(profileFeignClient.isActive(id)).thenReturn(true);

        assertTrue(adapter.isActive(id));
    }

    @Test
    void isActive_feignException_throwsExternalServiceException() {
        UUID id = UUID.randomUUID();
        when(profileFeignClient.isActive(id)).thenThrow(serverError("isActive"));

        assertThrows(ExternalServiceException.class, () -> adapter.isActive(id));
    }

}
