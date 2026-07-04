package co.edu.escuelaing.alphaeci.matching_service.application.mapper;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import co.edu.escuelaing.alphaeci.matching_service.application.dto.request.CreateCategoryRequest;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.request.CreateTagRequest;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.response.CategoryResponse;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.response.CategoryWithTagsResponse;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.response.TagResponse;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.Category;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.Tag;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryAppMapperTest {

    private CategoryAppMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(CategoryAppMapper.class);
    }

    // ======================== toResponse(Category) ========================

    @Test
    void toResponse_category_mapsCorrectly() {
        Category category = new Category("Sports");
        category.setId(UUID.randomUUID());

        CategoryResponse response = mapper.toResponse(category);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(category.getId());
        assertThat(response.getName()).isEqualTo(category.getName());
    }

    @Test
    void toResponse_categoryNull_returnsNull() {
        assertThat(mapper.toResponse((Category) null)).isNull();
    }

    // ======================== toResponseCategoryList ========================

    @Test
    void toResponseCategoryList_mapsList() {
        Category c1 = new Category("Music");
        c1.setId(UUID.randomUUID());
        Category c2 = new Category("Tech");
        c2.setId(UUID.randomUUID());

        List<CategoryResponse> result = mapper.toResponseCategoryList(List.of(c1, c2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("music");
        assertThat(result.get(1).getName()).isEqualTo("tech");
    }

    @Test
    void toResponseCategoryList_null_returnsNull() {
        assertThat(mapper.toResponseCategoryList(null)).isNull();
    }

    // ======================== toResponse(Tag) ========================

    @Test
    void toResponse_tag_mapsCorrectly() {
        UUID categoryId = UUID.randomUUID();
        Tag tag = new Tag("Rock", categoryId);
        tag.setId(UUID.randomUUID());

        TagResponse response = mapper.toResponse(tag);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(tag.getId());
        assertThat(response.getName()).isEqualTo(tag.getName());
        assertThat(response.getCategoryId()).isEqualTo(categoryId);
    }

    @Test
    void toResponse_tagNull_returnsNull() {
        assertThat(mapper.toResponse((Tag) null)).isNull();
    }

    // ======================== toResponseTagList ========================

    @Test
    void toResponseTagList_mapsList() {
        UUID catId = UUID.randomUUID();
        Tag t1 = new Tag("jazz", catId);
        Tag t2 = new Tag("blues", catId);

        List<TagResponse> result = mapper.toResponseTagList(List.of(t1, t2));

        assertThat(result).hasSize(2);
    }

    @Test
    void toResponseTagList_null_returnsNull() {
        assertThat(mapper.toResponseTagList(null)).isNull();
    }

    // ======================== toDomain(CreateCategoryRequest) ========================

    @Test
    void toDomain_createCategoryRequest_mapsCorrectly() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Art");

        Category result = mapper.toDomain(request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("art");
        assertThat(result.getId()).isNull();
    }

    @Test
    void toDomain_createCategoryRequest_null_returnsNull() {
        assertThat(mapper.toDomain((CreateCategoryRequest) null)).isNull();
    }

    // ======================== toDomain(CreateTagRequest) ========================

    @Test
    void toDomain_createTagRequest_mapsCorrectly() {
        UUID categoryId = UUID.randomUUID();
        CreateTagRequest request = new CreateTagRequest();
        request.setName("Hip-hop");
        request.setCategoryId(categoryId);

        Tag result = mapper.toDomain(request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("hip-hop");
        assertThat(result.getCategoryID()).isEqualTo(categoryId);
        assertThat(result.getId()).isNull();
    }

    @Test
    void toDomain_createTagRequest_null_returnsNull() {
        assertThat(mapper.toDomain((CreateTagRequest) null)).isNull();
    }

    // ======================== toResponseCategoryWithTags ========================

    @Test
    void toResponseCategoryWithTags_mapsCorrectly() {
        Category category = new Category("Music");
        category.setId(UUID.randomUUID());

        UUID catId = category.getId();
        Tag tag1 = new Tag("rock", catId);
        Tag tag2 = new Tag("jazz", catId);

        Map<Category, List<Tag>> input = new HashMap<>();
        input.put(category, List.of(tag1, tag2));

        List<CategoryWithTagsResponse> result = mapper.toResponseCategoryWithTags(input);

        assertThat(result).hasSize(1);
        CategoryWithTagsResponse response = result.get(0);
        assertThat(response.getId()).isEqualTo(category.getId());
        assertThat(response.getName()).isEqualTo(category.getName());
        assertThat(response.getTags()).hasSize(2);
    }

    @Test
    void toResponseCategoryWithTags_emptyMap_returnsEmptyList() {
        List<CategoryWithTagsResponse> result = mapper.toResponseCategoryWithTags(Collections.emptyMap());
        assertThat(result).isEmpty();
    }
}
