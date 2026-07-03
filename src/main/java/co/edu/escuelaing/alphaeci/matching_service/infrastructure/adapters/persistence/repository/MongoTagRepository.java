package co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

import co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.entity.TagDocument;

public interface MongoTagRepository extends MongoRepository<TagDocument, UUID> {

    Optional<TagDocument> findByName(String name);
    boolean existsByName(String name);
    List<TagDocument> findByCategoryID(UUID categoryID);

}
