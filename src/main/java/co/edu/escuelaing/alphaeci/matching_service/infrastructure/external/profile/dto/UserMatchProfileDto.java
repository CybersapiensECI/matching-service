package co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.profile.dto;

import java.util.List;

import lombok.Data;

/**
 * id is a String, not UUID: profile-service has seed/demo student records
 * whose id is a bare 32-hex string without dashes (not a valid
 * java.util.UUID). Typing this field as UUID makes Jackson reject the
 * *entire* list response the moment any one record has such an id,
 * failing matching for every user. Validate/parse at the adapter boundary
 * instead, where a single bad record can be skipped.
 */
@Data
public class UserMatchProfileDto {
    private String id;
    private String career; // ej: "ENGINEERING"
    private Integer semester;  // ej: 2
    private List<String> tags;  // ej : ["TAGid1", "TAGid2", "TAGid3"]
    private List<String> schedulesAvailable; // ej: ["MONDAY_8AM-10AM", "WEDNESDAY_2PM-4PM", "FRIDAY_10AM-12PM"]
    private boolean active;
}
