package co.edu.escuelaing.alphaeci.matching_service.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class SwaggerConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes(
                        BEARER_AUTH,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                ))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .info(new Info()
                        .title("Matching Service")
                        .version("1.0.0")
                        .description("""
                        Microservice responsible for match requests and user recommendation operations \
                        within the AlphaECI platform. Handles creation, retrieval and lifecycle \
                        management of matches, as well as recommendation queries based on affinity \
                        and geolocation.

                        **Authentication:** The API Gateway propagates the `X-User-Id` header \
                        with the authenticated user's ID. \

                        **Internal endpoints:** Routes under `/api/v1/internal` are consumed \
                        exclusively by other microservices and are not exposed through the \
                        public API gateway.
                        """)
                        .contact(new Contact()
                                .name("AlphaECI Team — Escuela Colombiana de Ingeniería Julio Garavito")
                                .email("crisiskitcheneci@gmail.com"))
                )
                .tags(Arrays.asList(
                        new Tag().name("Matches - Management")
                                .description("Create, update and cancel match requests between users. Includes endpoints to respond to requests and inspect match state."),
                        new Tag().name("Recommendations - Reading")
                                .description("Endpoints to obtain recommended users and scored/nearby recommendation lists."),
                        new Tag().name("Categories - Management")
                                .description("APIs to manage categories and tags used for user interests and recommendation matching.")
                ));
    }
}
