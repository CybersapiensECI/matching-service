package co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

import co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.entity.CategoryDocument;

public interface MongoCategoryRepository extends MongoRepository<CategoryDocument, UUID> {

    Optional<CategoryDocument> findByName(String name);
    boolean existsByName(String name);
}
