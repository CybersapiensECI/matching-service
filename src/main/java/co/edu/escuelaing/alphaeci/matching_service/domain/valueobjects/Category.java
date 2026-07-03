package co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects;

import java.util.UUID;

import co.edu.escuelaing.alphaeci.matching_service.domain.exceptions.InvalidInputException;
import lombok.Data;

@Data
public class Category {

    private UUID id;
    private String name;

    public Category(String name) {
        if (name == null || name.isBlank())
            throw new InvalidInputException("The category name cannot be blank");
        if (name.trim().length() > 50)
            throw new InvalidInputException("The category name must be between 1 and 50 characters");
        this.name = name.trim().toLowerCase();
    }

}