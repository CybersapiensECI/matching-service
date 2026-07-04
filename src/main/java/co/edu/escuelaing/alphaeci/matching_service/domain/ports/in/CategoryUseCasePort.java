package co.edu.escuelaing.alphaeci.matching_service.domain.ports.in;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.Category;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.Tag;



public interface CategoryUseCasePort {

    Category createCategory(String name);
    Category getCategory(UUID categoryId);
    List<Category> getAllCategories();
    Category updateCategory(UUID categoryId, String name);
    void deleteCategory(UUID categoryId);



    Tag createTag(String name, UUID categoryId);
    Tag getTag(UUID tagId);
    List<Tag> getTagsByCategoryId(UUID categoryId);
    Tag updateTag(UUID tagId, String name);
    void deleteTag(UUID tagId);
    List<Tag> getAllTags();


    Map<Category, List<Tag>> getAllCategoriesWithTags();

    Map<Category, List<Tag>> createCategoryWithTags(String name, List<String> tagNames);
}
