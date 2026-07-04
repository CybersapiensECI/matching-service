package co.edu.escuelaing.alphaeci.matching_service.infrastructure.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class KongAuthFilterTest {

    private KongAuthFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new KongAuthFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static String base64Url(String json) {
        return Base64.getUrlEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private static String bearerToken(String payloadJson) {
        return "Bearer header." + base64Url(payloadJson) + ".signature";
    }

    @Test
    void noAuthorizationHeader_continuesChainWithoutAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void authorizationHeaderNotBearer_continuesChainWithoutAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void malformedJwt_wrongNumberOfParts_continuesChainWithoutAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer only.twoparts");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void invalidBase64Payload_isCaughtAndChainContinues() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer header.not-valid-base64!!!.signature");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void invalidJsonPayload_isCaughtAndChainContinues() throws Exception {
        String token = bearerToken("not-a-json-object");
        when(request.getHeader("Authorization")).thenReturn(token);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void missingSubClaim_continuesChainWithoutAuthentication() throws Exception {
        String token = bearerToken("{\"role\":\"admin\"}");
        when(request.getHeader("Authorization")).thenReturn(token);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void blankSubClaim_continuesChainWithoutAuthentication() throws Exception {
        String token = bearerToken("{\"sub\":\"   \"}");
        when(request.getHeader("Authorization")).thenReturn(token);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void alreadyAuthenticated_doesNotOverwriteAuthentication() throws Exception {
        var existing = new UsernamePasswordAuthenticationToken("existingUser", null);
        SecurityContextHolder.getContext().setAuthentication(existing);

        String token = bearerToken("{\"sub\":\"newUser\"}");
        when(request.getHeader("Authorization")).thenReturn(token);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertSame(existing, SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void validTokenWithoutRoleClaims_defaultsToRoleUser() throws Exception {
        String userId = "11111111-1111-1111-1111-111111111111";
        String token = bearerToken("{\"sub\":\"" + userId + "\"}");
        when(request.getHeader("Authorization")).thenReturn(token);

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(userId, auth.getPrincipal());
        assertEquals(1, auth.getAuthorities().size());
        assertTrue(auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch("ROLE_USER"::equals));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void validTokenWithSingleRoleClaim_normalizesToRolePrefixUppercase() throws Exception {
        String token = bearerToken("{\"sub\":\"user1\",\"role\":\"admin\"}");
        when(request.getHeader("Authorization")).thenReturn(token);

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch("ROLE_ADMIN"::equals));
    }

    @Test
    void validTokenWithRoleAlreadyPrefixed_doesNotDoublePrefix() throws Exception {
        String token = bearerToken("{\"sub\":\"user1\",\"role\":\"Role_Admin\"}");
        when(request.getHeader("Authorization")).thenReturn(token);

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch("ROLE_ADMIN"::equals));
        assertFalse(auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(a -> a.equals("ROLE_ROLE_ADMIN")));
    }

    @Test
    void validTokenWithRolesArray_mapsAllRoles() throws Exception {
        String token = bearerToken("{\"sub\":\"user1\",\"roles\":[\"admin\",\"editor\",\"\"]}");
        when(request.getHeader("Authorization")).thenReturn(token);

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        var authorityNames = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertTrue(authorityNames.contains("ROLE_ADMIN"));
        assertTrue(authorityNames.contains("ROLE_EDITOR"));
        assertEquals(2, authorityNames.size());
    }

    @Test
    void validTokenWithBlankRoleClaim_defaultsToRoleUser() throws Exception {
        String token = bearerToken("{\"sub\":\"user1\",\"role\":\"   \"}");
        when(request.getHeader("Authorization")).thenReturn(token);

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch("ROLE_USER"::equals));
    }
}
