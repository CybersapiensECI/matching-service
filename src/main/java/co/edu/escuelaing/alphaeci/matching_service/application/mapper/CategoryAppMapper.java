package co.edu.escuelaing.alphaeci.matching_service.application.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.escuelaing.alphaeci.matching_service.application.dto.request.CreateCategoryRequest;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.request.CreateTagRequest;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.response.CategoryResponse;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.response.CategoryWithTagsResponse;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.response.TagResponse;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.Category;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.Tag;


@Mapper(componentModel = "spring")
public interface CategoryAppMapper {

    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toResponseCategoryList(List<Category> categories);

    @Mapping(target = "categoryId", source = "categoryID")
    TagResponse toResponse(Tag tag);

    List<TagResponse> toResponseTagList(List<Tag> tags);

    @Mapping(target = "id", ignore = true)
    Category toDomain(CreateCategoryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoryID", source = "categoryId")
    Tag toDomain(CreateTagRequest request);

    default List<CategoryWithTagsResponse> toResponseCategoryWithTags(Map<Category, List<Tag>> categoryWithTags) {
        List<CategoryWithTagsResponse> responses = new ArrayList<>();

        for (Map.Entry<Category, List<Tag>> entry : categoryWithTags.entrySet()) {
            responses.add(new CategoryWithTagsResponse(
                    entry.getKey().getId(),
                    entry.getKey().getName(),
                    toResponseTagList(entry.getValue())));
        }

        return responses;
    }

    default CategoryWithTagsResponse toSingleResponseCategoryWithTags(Map<Category, List<Tag>> categoryWithTags) {
        Map.Entry<Category, List<Tag>> entry = categoryWithTags.entrySet().iterator().next();
        return new CategoryWithTagsResponse(
                entry.getKey().getId(),
                entry.getKey().getName(),
                toResponseTagList(entry.getValue()));
    }
}
