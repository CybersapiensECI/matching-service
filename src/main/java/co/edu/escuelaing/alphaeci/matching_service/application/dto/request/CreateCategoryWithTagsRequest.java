package co.edu.escuelaing.alphaeci.matching_service.application.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CreateCategoryWithTagsRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    @NotEmpty(message = "At least one tag name is required")
    private List<@NotBlank(message = "Tag name must not be blank") String> tagNames;
}
