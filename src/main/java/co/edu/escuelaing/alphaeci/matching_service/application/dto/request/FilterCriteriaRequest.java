package co.edu.escuelaing.alphaeci.matching_service.application.dto.request;


import co.edu.escuelaing.alphaeci.matching_service.domain.model.enums.CareersEnum;
import co.edu.escuelaing.alphaeci.matching_service.domain.model.enums.SemesterEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterCriteriaRequest {
    private CareersEnum careers;      // ["SISTEMAS", "CIVIL"] — null = sin filtro
    private SemesterEnum semesters;   // [2, 3, 4] — null = sin filtro
    private String tag;  // ["gaming", "musica"] — null = sin filtro
    private boolean isGeolocation; // boolean
    private boolean isActive;  // boolean

}
