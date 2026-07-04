package co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.Category;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.Tag;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.entity.CategoryDocument;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.entity.TagDocument;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryPersistenceMapperTest {

    private CategoryPersistenceMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(CategoryPersistenceMapper.class);
    }

    // ======================== toDocument(Category) ========================

    @Test
    void toDocument_mapsCorrectly() {
        Category category = new Category("Tech");
        category.setId(UUID.randomUUID());

        CategoryDocument doc = mapper.toDocument(category);

        assertThat(doc).isNotNull();
        assertThat(doc.getId()).isEqualTo(category.getId());
        assertThat(doc.getName()).isEqualTo(category.getName());
    }

    @Test
    void toDocument_null_returnsNull() {
        assertThat(mapper.toDocument((Category) null)).isNull();
    }

    // ======================== toDomain(CategoryDocument) ========================

    @Test
    void toDomain_categoryDocument_mapsCorrectly() {
        UUID id = UUID.randomUUID();
        CategoryDocument doc = new CategoryDocument(id, "sports");

        Category result = mapper.toDomain(doc);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("sports");
    }

    @Test
    void toDomain_categoryDocument_null_returnsNull() {
        assertThat(mapper.toDomain((CategoryDocument) null)).isNull();
    }

    // ======================== toTagDocument ========================

    @Test
    void toTagDocument_mapsCorrectly() {
        UUID catId = UUID.randomUUID();
        Tag tag = new Tag("rock", catId);
        tag.setId(UUID.randomUUID());

        TagDocument doc = mapper.toTagDocument(tag);

        assertThat(doc).isNotNull();
        assertThat(doc.getId()).isEqualTo(tag.getId());
        assertThat(doc.getName()).isEqualTo(tag.getName());
        assertThat(doc.getCategoryID()).isEqualTo(catId);
    }

    @Test
    void toTagDocument_null_returnsNull() {
        assertThat(mapper.toTagDocument(null)).isNull();
    }

    // ======================== toTagDomain ========================

    @Test
    void toTagDomain_mapsCorrectly() {
        UUID tagId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        TagDocument doc = new TagDocument(tagId, "jazz", catId);

        Tag result = mapper.toTagDomain(doc);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(tagId);
        assertThat(result.getName()).isEqualTo("jazz");
        assertThat(result.getCategoryID()).isEqualTo(catId);
    }

    @Test
    void toTagDomain_null_returnsNull() {
        assertThat(mapper.toTagDomain(null)).isNull();
    }
}
