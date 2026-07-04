package co.edu.escuelaing.alphaeci.matching_service.domain.model;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchProfile {
    private UUID id;
    private String career;
    private Integer semester;
    private List<String> tags;
    private List<String> schedulesAvailable;
    private boolean active;
}