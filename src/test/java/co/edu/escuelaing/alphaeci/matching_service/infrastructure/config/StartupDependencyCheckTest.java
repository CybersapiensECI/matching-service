package co.edu.escuelaing.alphaeci.matching_service.infrastructure.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.ApplicationArguments;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.util.ReflectionTestUtils;

class StartupDependencyCheckTest {

    @Mock
    MongoTemplate mongoTemplate;

    @Mock
    ApplicationArguments applicationArguments;

    StartupDependencyCheck check;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        check = new StartupDependencyCheck(mongoTemplate);
    }

    @Test
    void run_failFastDisabled_skipsMongoCheck() throws Exception {
        ReflectionTestUtils.setField(check, "failFast", false);

        check.run(applicationArguments);

        verify(mongoTemplate, never()).executeCommand(any(Document.class));
    }

    @Test
    void run_failFastEnabled_mongoReachable_passes() throws Exception {
        ReflectionTestUtils.setField(check, "failFast", true);
        when(mongoTemplate.executeCommand(any(Document.class))).thenReturn(new Document("ok", 1.0));

        assertDoesNotThrow(() -> check.run(applicationArguments));

        verify(mongoTemplate).executeCommand(any(Document.class));
    }

    @Test
    void run_failFastEnabled_mongoUnreachable_throwsIllegalStateException() {
        ReflectionTestUtils.setField(check, "failFast", true);
        when(mongoTemplate.executeCommand(any(Document.class))).thenThrow(new RuntimeException("connection refused"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> check.run(applicationArguments));
        assertTrue(ex.getMessage().contains("MongoDB"));
    }
}
