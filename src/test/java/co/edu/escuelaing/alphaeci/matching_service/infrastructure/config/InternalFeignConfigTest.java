package co.edu.escuelaing.alphaeci.matching_service.infrastructure.config;

import static org.junit.jupiter.api.Assertions.*;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Guards the contract with profile-service's InternalServiceGuard: internal
 * calls must carry X-Internal-Key or they are rejected with 403.
 */
class InternalFeignConfigTest {

    InternalFeignConfig config;

    @BeforeEach
    void setUp() {
        config = new InternalFeignConfig();
    }

    private RequestTemplate applyInterceptor(String key) {
        ReflectionTestUtils.setField(config, "internalApiKey", key);
        RequestInterceptor interceptor = config.internalApiKeyInterceptor();
        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);
        return template;
    }

    @Test
    void interceptor_keyConfigured_addsInternalKeyHeader() {
        RequestTemplate template = applyInterceptor("super-secret");

        assertEquals(
                "super-secret",
                template.headers().get(InternalFeignConfig.INTERNAL_KEY_HEADER).iterator().next());
    }

    @Test
    void interceptor_keyMissing_doesNotAddHeader() {
        // Sending X-Internal-Key: "" would be indistinguishable from a real
        // mismatch; omit it so the 403 message names the actual cause.
        assertFalse(applyInterceptor("").headers().containsKey(InternalFeignConfig.INTERNAL_KEY_HEADER));
        assertFalse(applyInterceptor(null).headers().containsKey(InternalFeignConfig.INTERNAL_KEY_HEADER));
        assertFalse(applyInterceptor("   ").headers().containsKey(InternalFeignConfig.INTERNAL_KEY_HEADER));
    }
}
