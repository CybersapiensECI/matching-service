package co.edu.escuelaing.alphaeci.matching_service.application.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTagRequest {

    @NotBlank(message = "Tag name is required")
    private String name;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

}