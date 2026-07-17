package co.edu.escuelaing.alphaeci.matching_service.infrastructure.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * Feign configuration for clients that call profile-service's
 * {@code /api/v1/internal/**} routes.
 *
 * <p>Those routes are protected by profile-service's {@code InternalServiceGuard},
 * which requires the {@code X-Internal-Key} header to match its own
 * {@code INTERNAL_API_KEY} environment variable. Without this interceptor every
 * internal call fails with {@code 403 Forbidden}.
 *
 * <p>Deliberately NOT annotated with {@code @Configuration}: it lives inside the
 * component-scan path, and annotating it would register the interceptor globally,
 * leaking the internal key to unrelated clients (e.g. geolocation-service). It is
 * applied per-client via {@code @FeignClient(configuration = InternalFeignConfig.class)}.
 */
public class InternalFeignConfig {

    static final String INTERNAL_KEY_HEADER = "X-Internal-Key";

    @Value("${internal.api-key:}")
    private String internalApiKey;

    @Bean
    public RequestInterceptor internalApiKeyInterceptor() {
        return template -> {
            if (internalApiKey != null && !internalApiKey.isBlank()) {
                template.header(INTERNAL_KEY_HEADER, internalApiKey);
            }
        };
    }
}
