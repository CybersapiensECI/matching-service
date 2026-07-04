package co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.geolocation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
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
import co.edu.escuelaing.alphaeci.matching_service.domain.model.NearbyUserDistance;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.geolocation.client.GeolocationFeignClient;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.geolocation.dto.NearbyUserDto;
import feign.FeignException;
import feign.Request;
import feign.Response;

public class GeolocationServiceAdapterTest {

    @Mock
    GeolocationFeignClient geolocationFeignClient;

    GeolocationServiceAdapter adapter;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        adapter = new GeolocationServiceAdapter(geolocationFeignClient);
    }

    @Test
    void getNearbyUsers_mapsDtoToDomain() {
        UUID id = UUID.randomUUID();
        NearbyUserDto dto = new NearbyUserDto(id, 123.45, "zoneA");
        when(geolocationFeignClient.getNearbyUsers(eq(id), anyDouble(), anyBoolean())).thenReturn(List.of(dto));

        var result = adapter.getNearbyUsers(id);

        assertNotNull(result);
        assertEquals(1, result.size());
        NearbyUserDistance d = result.get(0);
        assertEquals(id, d.getUserId());
        assertEquals(123.45, d.getDistanceMeters(), 0.0001);
    }

    @Test
    void getNearbyUsers_feignException_throwsExternalServiceException() {
        UUID id = UUID.randomUUID();
        Request req = Request.create(Request.HttpMethod.GET, "http://x", java.util.Collections.emptyMap(), null, Charset.defaultCharset(), null);
        Response resp = Response.builder().status(500).reason("err").request(req).build();
        FeignException fe = FeignException.errorStatus("getNearbyUsers", resp);

        when(geolocationFeignClient.getNearbyUsers(eq(id), anyDouble(), anyBoolean())).thenThrow(fe);

        assertThrows(ExternalServiceException.class, () -> adapter.getNearbyUsers(id));
    }
}
