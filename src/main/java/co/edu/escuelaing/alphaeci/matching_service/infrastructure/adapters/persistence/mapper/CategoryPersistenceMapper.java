package co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.mapper;

import org.mapstruct.Mapper;

import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.Category;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.Tag;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.entity.CategoryDocument;
import co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.entity.TagDocument;


@Mapper(componentModel = "spring")
public interface CategoryPersistenceMapper {
    CategoryDocument toDocument(Category category);
    Category toDomain(CategoryDocument document);
    TagDocument toTagDocument(Tag tag);
    Tag toTagDomain(TagDocument document);
}
