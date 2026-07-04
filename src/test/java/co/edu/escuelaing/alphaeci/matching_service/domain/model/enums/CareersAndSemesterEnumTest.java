package co.edu.escuelaing.alphaeci.matching_service.domain.model.enums;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CareersAndSemesterEnumTest {

    @Test
    void careersEnum_values_and_valueOf() {
        CareersEnum[] values = CareersEnum.values();
        assertTrue(values.length >= 1, "Expected at least one career enum");
        // round-trip valueOf
        for (CareersEnum c : values) {
            assertEquals(c, CareersEnum.valueOf(c.name()));
            assertNotNull(c.toString());
        }
    }

    @Test
    void semesterEnum_values_and_valueOf() {
        SemesterEnum[] values = SemesterEnum.values();
        assertTrue(values.length >= 1, "Expected at least one semester enum");
        for (SemesterEnum s : values) {
            assertEquals(s, SemesterEnum.valueOf(s.name()));
            assertNotNull(s.toString());
        }
    }
}
