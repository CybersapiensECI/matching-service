package co.edu.escuelaing.alphaeci.matching_service.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartupDependencyCheck implements ApplicationRunner {

    private final MongoTemplate mongoTemplate;

    @Value("${app.startup.fail-fast:true}")
    private boolean failFast;

    @Value("${internal.api-key:}")
    private String internalApiKey;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Always warn: a missing internal key makes every profile-service call
        // fail with 403 at runtime, which is far harder to diagnose than a
        // loud message at boot.
        checkInternalApiKey();

        if (!failFast) {
            log.warn("Startup dependency checks are disabled (app.startup.fail-fast=false)");
            return;
        }

        checkMongo();
        log.info("Startup dependency checks passed: MongoDB is reachable");
    }

    private void checkInternalApiKey() {
        if (internalApiKey == null || internalApiKey.isBlank()) {
            String message = "INTERNAL_API_KEY is not set: every call to profile-service's "
                    + "/api/v1/internal/** routes will be rejected with 403 Forbidden. "
                    + "Set the INTERNAL_API_KEY environment variable to the same value "
                    + "configured in profile-service.";
            if (failFast) {
                throw new IllegalStateException("Startup failed: " + message);
            }
            log.error(message);
        }
    }

    private void checkMongo() {
        try {
            mongoTemplate.executeCommand(new Document("ping", 1));
        } catch (Exception ex) {
            throw new IllegalStateException("Startup failed: cannot connect to MongoDB", ex);
        }
    }

}
