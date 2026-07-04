package co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects;

import java.util.UUID;

import co.edu.escuelaing.alphaeci.matching_service.domain.exceptions.InvalidInputException;
import lombok.Data;

@Data
public class Tag {

    private UUID id;
    private String name;
    private UUID categoryID;

    public Tag(String name, UUID categoryID) {
        if (name == null || name.isBlank()) throw new InvalidInputException("The tag name cannot be blank");
        if (categoryID == null) throw new InvalidInputException("The tag category ID cannot be null");
        if (name.trim().length() > 50) throw new InvalidInputException("The tag name must be between 1 and 50 characters");

        this.name = name.trim().toLowerCase();
        this.categoryID = categoryID;
    }

}
