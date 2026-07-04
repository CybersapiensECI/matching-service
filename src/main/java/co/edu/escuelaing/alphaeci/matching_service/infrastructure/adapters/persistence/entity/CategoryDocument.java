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
public class CategoryDocument {
    @Id
    private UUID id;

    @Indexed(unique = true)
    private String name;
}
