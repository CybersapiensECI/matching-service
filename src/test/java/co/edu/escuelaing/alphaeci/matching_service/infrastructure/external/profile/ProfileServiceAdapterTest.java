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
        dto.setId(id);
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

}
