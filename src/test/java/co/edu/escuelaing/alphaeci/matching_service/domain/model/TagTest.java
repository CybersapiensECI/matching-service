package co.edu.escuelaing.alphaeci.matching_service.domain.model;

import org.junit.jupiter.api.Test;

import co.edu.escuelaing.alphaeci.matching_service.domain.exceptions.InvalidInputException;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.Tag;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class TagTest {

    @Test
    void constructor_validArgs_normalizesName() {
        UUID catId = UUID.randomUUID();
        Tag tag = new Tag("  Java  ", catId);
        assertThat(tag.getName()).isEqualTo("java");
        assertThat(tag.getCategoryID()).isEqualTo(catId);
    }

    @Test
    void constructor_nullName_throwsInvalidInputException() {
        assertThatThrownBy(() -> new Tag(null, UUID.randomUUID()))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("cannot be blank");
    }

    @Test
    void constructor_blankName_throwsInvalidInputException() {
        assertThatThrownBy(() -> new Tag("", UUID.randomUUID()))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void constructor_nullCategoryId_throwsInvalidInputException() {
        assertThatThrownBy(() -> new Tag("Java", null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("category ID cannot be null");
    }

    @Test
    void constructor_nameTooLong_throwsInvalidInputException() {
        String longName = "a".repeat(51);
        assertThatThrownBy(() -> new Tag(longName, UUID.randomUUID()))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("50 characters");
    }

    @Test
    void constructor_nameExactly50Chars_doesNotThrow() {
        String name = "a".repeat(50);
        assertThatCode(() -> new Tag(name, UUID.randomUUID())).doesNotThrowAnyException();
    }

    @Test
    void setId_andGetId_work() {
        Tag tag = new Tag("Java", UUID.randomUUID());
        UUID id = UUID.randomUUID();
        tag.setId(id);
        assertThat(tag.getId()).isEqualTo(id);
    }
}
