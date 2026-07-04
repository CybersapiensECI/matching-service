package co.edu.escuelaing.alphaeci.matching_service.domain.ports.out;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.Category;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.Tag;


public interface CategoryRepositoryPort {

    Category save(Category category);
    Optional<Category> findById(UUID id);
    Optional<Category> findByName(String name);
    List<Category> findAll();
    boolean existsById(UUID id);
    boolean existsByName(String name);
    void deleteById(UUID id);

    Tag save(Tag tag);
    Optional<Tag> findTagById(UUID id);
    List<Tag> findAllTags();
    List<Tag> findTagsByCategoryId(UUID categoryId);
    boolean existsTagById(UUID id);
    boolean existsTagByName(String name);
    void deleteTagById(UUID id);

    Map<Category, List<Tag>> findAllCategoriesWithTags();

}
