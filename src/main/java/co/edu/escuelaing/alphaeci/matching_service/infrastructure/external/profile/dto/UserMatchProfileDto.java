package co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.profile.dto;

import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class UserMatchProfileDto {
    private UUID id;
    private String career; // ej: "ENGINEERING"
    private Integer semester;  // ej: 2
    private List<String> tags;  // ej : ["TAGid1", "TAGid2", "TAGid3"]
    private List<String> schedulesAvailable; // ej: ["MONDAY_8AM-10AM", "WEDNESDAY_2PM-4PM", "FRIDAY_10AM-12PM"]
    private boolean active;
}
