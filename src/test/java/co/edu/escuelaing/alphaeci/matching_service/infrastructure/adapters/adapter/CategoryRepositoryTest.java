package co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.Category;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.Tag;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.entity.CategoryDocument;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.entity.TagDocument;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.mapper.CategoryPersistenceMapper;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.repository.MongoCategoryRepository;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.repository.MongoTagRepository;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryRepositoryTest {

    @Mock
    private MongoCategoryRepository mongoCategoryRepository;

    @Mock
    private MongoTagRepository mongoTagRepository;

    @Mock
    private CategoryPersistenceMapper mapper;

    @InjectMocks
    private CategoryRepository categoryRepository;

    private Category buildCategory(String name) {
        Category c = new Category(name);
        c.setId(UUID.randomUUID());
        return c;
    }

    private Tag buildTag(String name) {
        UUID catId = UUID.randomUUID();
        Tag t = new Tag(name, catId);
        t.setId(UUID.randomUUID());
        return t;
    }

    // ======================== save(Category) ========================

    @Test
    void save_category_returnsMappedDomain() {
        Category category = buildCategory("sports");
        CategoryDocument doc = new CategoryDocument(category.getId(), category.getName());
        when(mapper.toDocument(category)).thenReturn(doc);
        when(mongoCategoryRepository.save(doc)).thenReturn(doc);
        when(mapper.toDomain(doc)).thenReturn(category);

        Category result = categoryRepository.save(category);

        assertThat(result).isEqualTo(category);
        verify(mongoCategoryRepository).save(doc);
    }

    // ======================== findById ========================

    @Test
    void findById_found_returnsMappedCategory() {
        UUID id = UUID.randomUUID();
        CategoryDocument doc = new CategoryDocument(id, "music");
        Category category = buildCategory("music");
        when(mongoCategoryRepository.findById(id)).thenReturn(Optional.of(doc));
        when(mapper.toDomain(doc)).thenReturn(category);

        Optional<Category> result = categoryRepository.findById(id);

        assertThat(result).isPresent().contains(category);
    }

    @Test
    void findById_notFound_returnsEmpty() {
        UUID id = UUID.randomUUID();
        when(mongoCategoryRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Category> result = categoryRepository.findById(id);

        assertThat(result).isEmpty();
    }

    // ======================== findAll ========================

    @Test
    void findAll_returnsMappedList() {
        CategoryDocument doc1 = new CategoryDocument(UUID.randomUUID(), "tech");
        CategoryDocument doc2 = new CategoryDocument(UUID.randomUUID(), "art");
        Category c1 = buildCategory("tech");
        Category c2 = buildCategory("art");
        when(mongoCategoryRepository.findAll()).thenReturn(List.of(doc1, doc2));
        when(mapper.toDomain(doc1)).thenReturn(c1);
        when(mapper.toDomain(doc2)).thenReturn(c2);

        List<Category> result = categoryRepository.findAll();

        assertThat(result).containsExactly(c1, c2);
    }

    // ======================== save(Tag) ========================

    @Test
    void save_tag_returnsMappedDomain() {
        Tag tag = buildTag("jazz");
        TagDocument doc = new TagDocument(tag.getId(), tag.getName(), tag.getCategoryID());
        when(mapper.toTagDocument(tag)).thenReturn(doc);
        when(mongoTagRepository.save(doc)).thenReturn(doc);
        when(mapper.toTagDomain(doc)).thenReturn(tag);

        Tag result = categoryRepository.save(tag);

        assertThat(result).isEqualTo(tag);
    }

    // ======================== findTagById ========================

    @Test
    void findTagById_found_returnsMappedTag() {
        UUID id = UUID.randomUUID();
        Tag tag = buildTag("blues");
        TagDocument doc = new TagDocument(id, "blues", UUID.randomUUID());
        when(mongoTagRepository.findById(id)).thenReturn(Optional.of(doc));
        when(mapper.toTagDomain(doc)).thenReturn(tag);

        Optional<Tag> result = categoryRepository.findTagById(id);

        assertThat(result).isPresent().contains(tag);
    }

    @Test
    void findTagById_notFound_returnsEmpty() {
        UUID id = UUID.randomUUID();
        when(mongoTagRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Tag> result = categoryRepository.findTagById(id);

        assertThat(result).isEmpty();
    }

    // ======================== findAllTags ========================

    @Test
    void findAllTags_returnsMappedList() {
        TagDocument doc1 = new TagDocument(UUID.randomUUID(), "rock", UUID.randomUUID());
        Tag tag1 = buildTag("rock");
        when(mongoTagRepository.findAll()).thenReturn(List.of(doc1));
        when(mapper.toTagDomain(doc1)).thenReturn(tag1);

        List<Tag> result = categoryRepository.findAllTags();

        assertThat(result).containsExactly(tag1);
    }

    // ======================== existsById ========================

    @Test
    void existsById_delegatesToRepository() {
        UUID id = UUID.randomUUID();
        when(mongoCategoryRepository.existsById(id)).thenReturn(true);

        assertThat(categoryRepository.existsById(id)).isTrue();
    }

    // ======================== existsByName ========================

    @Test
    void existsByName_delegatesToRepository() {
        when(mongoCategoryRepository.existsByName("tech")).thenReturn(true);

        assertThat(categoryRepository.existsByName("tech")).isTrue();
    }

    // ======================== deleteById ========================

    @Test
    void deleteById_callsRepository() {
        UUID id = UUID.randomUUID();
        categoryRepository.deleteById(id);
        verify(mongoCategoryRepository).deleteById(id);
    }

    // ======================== findTagsByCategoryId ========================

    @Test
    void findTagsByCategoryId_returnsMappedTags() {
        UUID categoryId = UUID.randomUUID();
        TagDocument doc = new TagDocument(UUID.randomUUID(), "hip-hop", categoryId);
        Tag tag = buildTag("hip-hop");
        when(mongoTagRepository.findByCategoryID(categoryId)).thenReturn(List.of(doc));
        when(mapper.toTagDomain(doc)).thenReturn(tag);

        List<Tag> result = categoryRepository.findTagsByCategoryId(categoryId);

        assertThat(result).containsExactly(tag);
    }

    // ======================== existsTagById ========================

    @Test
    void existsTagById_delegatesToRepository() {
        UUID id = UUID.randomUUID();
        when(mongoTagRepository.existsById(id)).thenReturn(false);

        assertThat(categoryRepository.existsTagById(id)).isFalse();
    }

    // ======================== existsTagByName ========================

    @Test
    void existsTagByName_delegatesToRepository() {
        when(mongoTagRepository.existsByName("rock")).thenReturn(true);

        assertThat(categoryRepository.existsTagByName("rock")).isTrue();
    }

    // ======================== deleteTagById ========================

    @Test
    void deleteTagById_callsRepository() {
        UUID id = UUID.randomUUID();
        categoryRepository.deleteTagById(id);
        verify(mongoTagRepository).deleteById(id);
    }

    // ======================== findAllCategoriesWithTags ========================

    @Test
    void findAllCategoriesWithTags_groupsTagsByCategory() {
        UUID catId = UUID.randomUUID();
        CategoryDocument catDoc = new CategoryDocument(catId, "music");
        Category category = new Category("music");
        category.setId(catId);

        UUID tagId = UUID.randomUUID();
        TagDocument tagDoc = new TagDocument(tagId, "rock", catId);
        Tag tag = new Tag("rock", catId);
        tag.setId(tagId);

        when(mongoCategoryRepository.findAll()).thenReturn(List.of(catDoc));
        when(mapper.toDomain(catDoc)).thenReturn(category);
        when(mongoTagRepository.findAll()).thenReturn(List.of(tagDoc));
        when(mapper.toTagDomain(tagDoc)).thenReturn(tag);

        Map<Category, List<Tag>> result = categoryRepository.findAllCategoriesWithTags();

        assertThat(result).hasSize(1);
        assertThat(result.get(category)).containsExactly(tag);
    }

    @Test
    void findAllCategoriesWithTags_categoryWithNoTags_getsEmptyList() {
        UUID catId = UUID.randomUUID();
        CategoryDocument catDoc = new CategoryDocument(catId, "empty-cat");
        Category category = new Category("empty-cat");
        category.setId(catId);

        when(mongoCategoryRepository.findAll()).thenReturn(List.of(catDoc));
        when(mapper.toDomain(catDoc)).thenReturn(category);
        when(mongoTagRepository.findAll()).thenReturn(Collections.emptyList());

        Map<Category, List<Tag>> result = categoryRepository.findAllCategoriesWithTags();

        assertThat(result).hasSize(1);
        assertThat(result.get(category)).isEmpty();
    }
}
