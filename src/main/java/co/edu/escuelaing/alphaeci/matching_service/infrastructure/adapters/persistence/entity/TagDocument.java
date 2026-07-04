package co.edu.escuelaing.alphaeci.matching_service.infrastructure.adapters.persistence.entity;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagDocument {

    @Id
    private UUID id;

    @Indexed(unique = true)
    private String name;
    @Indexed
    private UUID categoryID;
}
