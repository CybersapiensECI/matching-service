package co.edu.escuelaing.alphaeci.matching_service.application.usecase;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.escuelaing.alphaeci.matching_service.domain.exceptions.InvalidInputException;
import co.edu.escuelaing.alphaeci.matching_service.domain.exceptions.NotFoundException;
import co.edu.escuelaing.alphaeci.matching_service.domain.ports.in.CategoryUseCasePort;
import co.edu.escuelaing.alphaeci.matching_service.domain.ports.out.CategoryRepositoryPort;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.Category;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.Tag;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryUseCasePort {

    private final CategoryRepositoryPort categoryRepository;

    @Override
    public Category createCategory(String name) {
        String normalizedName = normalizeName(name);
        existsCategoryByName(normalizedName);
        Category category = new Category(name);
        category.setId(UUID.randomUUID());
        return categoryRepository.save(category);
    }

    @Override
    public Category getCategory(UUID categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() -> new NotFoundException("Category not found with ID: " + categoryId));
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category updateCategory(UUID categoryId, String name) {
        existsCategoryById(categoryId);
        String normalizedName = normalizeName(name);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found with ID: " + categoryId));
        if (!category.getName().equals(normalizedName)) {
            existsCategoryByName(normalizedName);
        }
        category.setName(name);
        return categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(UUID categoryId) {
        existsCategoryById(categoryId);
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public Tag createTag(String name, UUID categoryId) {
        existsCategoryById(categoryId);
        String normalizedName = normalizeName(name);
        existsTagByName(normalizedName);
        Tag tag = new Tag(name, categoryId);
        tag.setId(UUID.randomUUID());
        return categoryRepository.save(tag);
    }

    @Override
    public Tag getTag(UUID tagId) {
        return categoryRepository.findTagById(tagId).orElseThrow(() -> new NotFoundException("Tag not found with ID: " + tagId));
    }

    @Override
    public List<Tag> getTagsByCategoryId(UUID categoryId) {
        existsCategoryById(categoryId);
        return categoryRepository.findTagsByCategoryId(categoryId);
    }

    @Override
    public Tag updateTag(UUID tagId, String name) {
        existsTagById(tagId);
        String normalizedName = normalizeName(name);
        Tag tag = categoryRepository.findTagById(tagId)
                .orElseThrow(() -> new NotFoundException("Tag not found with ID: " + tagId));
        if (!tag.getName().equals(normalizedName)) {
            existsTagByName(normalizedName);
        }
        tag.setName(name);
        return categoryRepository.save(tag);
    }

    @Override
    public void deleteTag(UUID tagId) {
        existsTagById(tagId);
        categoryRepository.deleteTagById(tagId);
    }

    // -- Helper methods for category existence checks, etc. --

    private void existsCategoryById(UUID categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException("Category not found with ID: " + categoryId);
        }
    }

    private void existsCategoryByName(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new InvalidInputException("Category already exists with name: " + name);
        }
    }

    private String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        return name.trim().toLowerCase();
    }

    private void existsTagById(UUID tagId) {
        if (!categoryRepository.existsTagById(tagId)) {
            throw new NotFoundException("Tag not found with ID: " + tagId);
        }
    }

    private void existsTagByName(String name) {
        if (categoryRepository.existsTagByName(name)) {
            throw new InvalidInputException("Tag already exists with name: " + name);
        }
    }

    @Override
    public List<Tag> getAllTags() {
        return categoryRepository.findAllTags();
    }

    @Override
    public Map<Category, List<Tag>> getAllCategoriesWithTags() {
        return categoryRepository.findAllCategoriesWithTags();
    }

    @Override
    public Map<Category, List<Tag>> createCategoryWithTags(String name, List<String> tagNames) {
        String normalizedName = normalizeName(name);

        Category category = categoryRepository.findByName(normalizedName)
                .orElseGet(() -> {
                    return createCategory(name);
                });

        for (String tagName : tagNames) {
            String normalizedTagName = normalizeName(tagName);
            if (!categoryRepository.existsTagByName(normalizedTagName)) {
                createTag(tagName, category.getId());
            }
        }

        return Map.of(category, categoryRepository.findTagsByCategoryId(category.getId()));
    }

}
