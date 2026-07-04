package co.edu.escuelaing.alphaeci.matching_service.application.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TagResponse {

    private UUID id;
    private String name;
    private UUID categoryId;

}