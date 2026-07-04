package co.edu.escuelaing.alphaeci.matching_service.application.dto.response;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryWithTagsResponse {
    private UUID id;
    private String name;
    private List<TagResponse> tags;
}
