package co.edu.escuelaing.alphaeci.matching_service.infrastructure.config;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class KongAuthFilter extends OncePerRequestFilter {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Value("${app.jwt.secret}")
	private String jwtSecret;

	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain) throws ServletException, IOException {
		String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			String token = authHeader.substring(7);
			String[] parts = token.split("\\.");

			if (parts.length != 3) {
				filterChain.doFilter(request, response);
				return;
			}

			if (!signatureValid(parts)) {
				log.warn("JWT con firma invalida");
				filterChain.doFilter(request, response);
				return;
			}

			byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
			String json = new String(decoded, StandardCharsets.UTF_8);
			JsonNode claims = objectMapper.readTree(json);

			String userId = readTextClaim(claims, "sub").orElse(null);
			if (userId == null || SecurityContextHolder.getContext().getAuthentication() != null) {
				filterChain.doFilter(request, response);
				return;
			}

			List<SimpleGrantedAuthority> authorities = readAuthorities(claims);

			UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(userId, null, authorities);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} catch (Exception e) {
			log.warn("JWT inválido: {}", e.getMessage());
		}

		filterChain.doFilter(request, response);
	}

	private boolean signatureValid(String[] parts) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			byte[] hash = mac.doFinal((parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8));
			String expectedSignature = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
			return expectedSignature.equals(parts[2]);
		} catch (Exception e) {
			return false;
		}
	}

	private Optional<String> readTextClaim(JsonNode claims, String claimName) {
		if (!claims.hasNonNull(claimName)) {
			return Optional.empty();
		}

		String value = claims.get(claimName).asText();
		if (value == null || value.isBlank()) {
			return Optional.empty();
		}

		return Optional.of(value);
	}

	private List<SimpleGrantedAuthority> readAuthorities(JsonNode claims) {
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();

		if (claims.has("role") && claims.get("role").isTextual()) {
			String role = claims.get("role").asText().trim();
			if (!role.isEmpty()) {
				authorities.add(new SimpleGrantedAuthority(normalizeRole(role)));
			}
		}

		if (claims.has("roles") && claims.get("roles").isArray()) {
			claims.get("roles").forEach(roleNode -> {
				String role = roleNode.asText().trim();
				if (!role.isEmpty()) {
					authorities.add(new SimpleGrantedAuthority(normalizeRole(role)));
				}
			});
		}

		if (authorities.isEmpty()) {
			authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		}

		return authorities;
	}

	private String normalizeRole(String role) {
		return role.toUpperCase().startsWith("ROLE_") ? role.toUpperCase() : "ROLE_" + role.toUpperCase();
	}

}
