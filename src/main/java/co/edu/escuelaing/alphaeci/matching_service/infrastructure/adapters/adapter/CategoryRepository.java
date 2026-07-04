package co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.adapter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import co.edu.escuelaing.alphaeci.matching_service.domain.ports.out.CategoryRepositoryPort;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.Category;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.Tag;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.mapper.CategoryPersistenceMapper;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.repository.MongoCategoryRepository;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.repository.MongoTagRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CategoryRepository implements CategoryRepositoryPort {

    private final MongoCategoryRepository mongoCategoryRepository;
    private final MongoTagRepository mongoTagRepository;
    private final CategoryPersistenceMapper mapper;

    @Override
    public Category save(Category category) {
        return mapper.toDomain(mongoCategoryRepository.save(mapper.toDocument(category)));
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return mongoCategoryRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Category> findByName(String name) {
        return mongoCategoryRepository.findByName(name)
                .map(mapper::toDomain);
    }

    @Override
    public List<Category> findAll() {
        return mongoCategoryRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Tag save(Tag tag) {
        return mapper.toTagDomain(mongoTagRepository.save(mapper.toTagDocument(tag)));
    }

    @Override
    public Optional<Tag> findTagById(UUID id) {
        return mongoTagRepository.findById(id)
                .map(mapper::toTagDomain);
    }

    @Override
    public List<Tag> findAllTags() {
        return mongoTagRepository.findAll().stream()
                .map(mapper::toTagDomain)
                .toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return mongoCategoryRepository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return mongoCategoryRepository.existsByName(name);
    }

    @Override
    public void deleteById(UUID id) {
        mongoCategoryRepository.deleteById(id);
    }

    @Override
    public List<Tag> findTagsByCategoryId(UUID categoryId) {
        return mongoTagRepository.findByCategoryID(categoryId).stream()
                .map(mapper::toTagDomain)
                .toList();
    }

    @Override
    public boolean existsTagById(UUID id) {
        return mongoTagRepository.existsById(id);
    }

    @Override
    public boolean existsTagByName(String name) {
        return mongoTagRepository.existsByName(name);
    }

    @Override
    public void deleteTagById(UUID id) {
        mongoTagRepository.deleteById(id);
    }

    @Override
    public Map<Category, List<Tag>> findAllCategoriesWithTags() {
        List<Category> categories = findAll();
        Map<UUID, List<Tag>> tagsByCategoryId = mongoTagRepository.findAll().stream()
                .map(mapper::toTagDomain)
                .collect(Collectors.groupingBy(Tag::getCategoryID));

        return categories.stream()
                .collect(Collectors.toMap(
                        category -> category,
                        category -> tagsByCategoryId.getOrDefault(category.getId(), List.of())
                ));
    }
}
