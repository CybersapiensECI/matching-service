package co.edu.escuelaing.alphaeci.matching_service.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.escuelaing.alphaeci.matching_service.domain.model.MatchProfile;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.AffinityScore;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AffinityCalculatorImplTest {

    private AffinityCalculatorImpl calculator;

    @BeforeEach
    void setUp() {
        calculator = new AffinityCalculatorImpl();
    }

    private MatchProfile buildProfile(UUID id, String career, Integer semester,
                                      List<String> tags, List<String> schedules) {
        return new MatchProfile(id, career, semester, tags, schedules, true);
    }

    @Test
    void calculate_perfectMatch_returnsHighScore() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        MatchProfile a = buildProfile(id1, "Engineering", 3,
                Arrays.asList("java", "spring"), Arrays.asList("MONDAY_8AM-10AM", "FRIDAY_2PM-4PM"));
        MatchProfile b = buildProfile(id2, "Engineering", 3,
                Arrays.asList("java", "spring"), Arrays.asList("MONDAY_8AM-10AM", "FRIDAY_2PM-4PM"));

        AffinityScore score = calculator.calculate(a, b);

        assertThat(score.getTotalScore()).isGreaterThan(0.9);
        assertThat(score.getInterestScore()).isEqualTo(1.0);
        assertThat(score.getScheduleScore()).isEqualTo(1.0);
        assertThat(score.getAcademicScore()).isGreaterThan(0.9);
    }

    @Test
    void calculate_noCommonTags_returnsZeroInterestScore() {
        MatchProfile a = buildProfile(UUID.randomUUID(), "Engineering", 3,
                Arrays.asList("java", "spring"), Arrays.asList("MONDAY_8AM-10AM"));
        MatchProfile b = buildProfile(UUID.randomUUID(), "Engineering", 3,
                Arrays.asList("python", "django"), Arrays.asList("MONDAY_8AM-10AM"));

        AffinityScore score = calculator.calculate(a, b);

        assertThat(score.getInterestScore()).isEqualTo(0.0);
    }

    @Test
    void calculate_bothEmptyTags_returnsZeroInterestScore() {
        MatchProfile a = buildProfile(UUID.randomUUID(), "Engineering", 3,
                Collections.emptyList(), Arrays.asList("MONDAY_8AM-10AM"));
        MatchProfile b = buildProfile(UUID.randomUUID(), "Engineering", 3,
                Collections.emptyList(), Arrays.asList("MONDAY_8AM-10AM"));

        AffinityScore score = calculator.calculate(a, b);

        assertThat(score.getInterestScore()).isEqualTo(0.0);
    }

    @Test
    void calculate_nullTags_returnsZeroInterestScore() {
        MatchProfile a = buildProfile(UUID.randomUUID(), "Engineering", 3,
                null, Arrays.asList("MONDAY_8AM-10AM"));
        MatchProfile b = buildProfile(UUID.randomUUID(), "Engineering", 3,
                null, Arrays.asList("MONDAY_8AM-10AM"));

        AffinityScore score = calculator.calculate(a, b);

        assertThat(score.getInterestScore()).isEqualTo(0.0);
    }

    @Test
    void calculate_partialTagMatch_returnsJaccardScore() {
        MatchProfile a = buildProfile(UUID.randomUUID(), "Engineering", 3,
                Arrays.asList("java", "spring", "docker"), Arrays.asList("MONDAY_8AM-10AM"));
        MatchProfile b = buildProfile(UUID.randomUUID(), "Engineering", 3,
                Arrays.asList("java", "python"), Arrays.asList("MONDAY_8AM-10AM"));

        AffinityScore score = calculator.calculate(a, b);

        // intersection=1 (java), union=4 (java,spring,docker,python) => 0.25
        assertThat(score.getInterestScore()).isEqualTo(0.25);
    }

    @Test
    void calculate_sameCareer_getsCareerBonus() {
        MatchProfile a = buildProfile(UUID.randomUUID(), "Engineering", 5,
                Collections.emptyList(), Collections.emptyList());
        MatchProfile b = buildProfile(UUID.randomUUID(), "Engineering", 5,
                Collections.emptyList(), Collections.emptyList());

        AffinityScore score = calculator.calculate(a, b);

        // academicScore = 0.6*1.0 + 0.4*1.0 = 1.0
        assertThat(score.getAcademicScore()).isEqualTo(1.0);
    }

    @Test
    void calculate_differentCareer_noCareerBonus() {
        MatchProfile a = buildProfile(UUID.randomUUID(), "Engineering", 5,
                Collections.emptyList(), Collections.emptyList());
        MatchProfile b = buildProfile(UUID.randomUUID(), "Medicine", 5,
                Collections.emptyList(), Collections.emptyList());

        AffinityScore score = calculator.calculate(a, b);

        // careerScore=0.0, semesterScore=1.0 => 0.6*0 + 0.4*1.0 = 0.4
        assertThat(score.getAcademicScore()).isEqualTo(0.4);
    }

    @Test
    void calculate_nullSemesters_semesterScoreZero() {
        MatchProfile a = buildProfile(UUID.randomUUID(), "Engineering", null,
                Collections.emptyList(), Collections.emptyList());
        MatchProfile b = buildProfile(UUID.randomUUID(), "Engineering", null,
                Collections.emptyList(), Collections.emptyList());

        AffinityScore score = calculator.calculate(a, b);

        // semesterScore=0.0, careerScore=1.0 => 0.6*1.0 + 0.4*0.0 = 0.6
        assertThat(score.getAcademicScore()).isEqualTo(0.6);
    }

    @Test
    void calculate_largeSemesterDiff_semesterScoreZero() {
        MatchProfile a = buildProfile(UUID.randomUUID(), "Engineering", 1,
                Collections.emptyList(), Collections.emptyList());
        MatchProfile b = buildProfile(UUID.randomUUID(), "Engineering", 10,
                Collections.emptyList(), Collections.emptyList());

        AffinityScore score = calculator.calculate(a, b);

        // diff=9, MAX=9 => semesterScore = max(0, 1 - 9/9) = 0.0
        // academic = 0.6*1.0 + 0.4*0.0 = 0.6
        assertThat(score.getAcademicScore()).isEqualTo(0.6);
    }

    @Test
    void calculate_noCommonSchedules_returnsZeroScheduleScore() {
        MatchProfile a = buildProfile(UUID.randomUUID(), "Engineering", 3,
                Collections.emptyList(), Arrays.asList("MONDAY_8AM-10AM"));
        MatchProfile b = buildProfile(UUID.randomUUID(), "Engineering", 3,
                Collections.emptyList(), Arrays.asList("FRIDAY_2PM-4PM"));

        AffinityScore score = calculator.calculate(a, b);

        assertThat(score.getScheduleScore()).isEqualTo(0.0);
    }

    @Test
    void calculate_emptySchedules_returnsZeroScheduleScore() {
        MatchProfile a = buildProfile(UUID.randomUUID(), "Engineering", 3,
                Collections.emptyList(), Collections.emptyList());
        MatchProfile b = buildProfile(UUID.randomUUID(), "Engineering", 3,
                Collections.emptyList(), Arrays.asList("FRIDAY_2PM-4PM"));

        AffinityScore score = calculator.calculate(a, b);

        assertThat(score.getScheduleScore()).isEqualTo(0.0);
    }

    @Test
    void calculate_nullSchedules_returnsZeroScheduleScore() {
        MatchProfile a = buildProfile(UUID.randomUUID(), "Engineering", 3,
                Collections.emptyList(), null);
        MatchProfile b = buildProfile(UUID.randomUUID(), "Engineering", 3,
                Collections.emptyList(), null);

        AffinityScore score = calculator.calculate(a, b);

        assertThat(score.getScheduleScore()).isEqualTo(0.0);
    }

    @Test
    void calculate_caseInsensitiveTags_matchesCorrectly() {
        MatchProfile a = buildProfile(UUID.randomUUID(), "Engineering", 3,
                Arrays.asList("Java", "SPRING"), Arrays.asList("MONDAY_8AM-10AM"));
        MatchProfile b = buildProfile(UUID.randomUUID(), "Engineering", 3,
                Arrays.asList("java", "spring"), Arrays.asList("MONDAY_8AM-10AM"));

        AffinityScore score = calculator.calculate(a, b);

        assertThat(score.getInterestScore()).isEqualTo(1.0);
    }

    @Test
    void calculate_caseInsensitiveCareer_matchesCorrectly() {
        MatchProfile a = buildProfile(UUID.randomUUID(), "ENGINEERING", 3,
                Collections.emptyList(), Collections.emptyList());
        MatchProfile b = buildProfile(UUID.randomUUID(), "engineering", 3,
                Collections.emptyList(), Collections.emptyList());

        AffinityScore score = calculator.calculate(a, b);

        assertThat(score.getAcademicScore()).isGreaterThan(0.5);
    }

    @Test
    void calculate_nullCareer_returnsZeroCareerScore() {
        MatchProfile a = buildProfile(UUID.randomUUID(), null, 3,
                Collections.emptyList(), Collections.emptyList());
        MatchProfile b = buildProfile(UUID.randomUUID(), "Engineering", 3,
                Collections.emptyList(), Collections.emptyList());

        AffinityScore score = calculator.calculate(a, b);

        // careerScore=0.0
        assertThat(score.getAcademicScore()).isLessThan(1.0);
    }

    @Test
    void calculate_totalScoreIsWeightedAverage() {
        MatchProfile a = buildProfile(UUID.randomUUID(), "Engineering", 3,
                Arrays.asList("java"), Arrays.asList("MONDAY_8AM-10AM"));
        MatchProfile b = buildProfile(UUID.randomUUID(), "Engineering", 3,
                Arrays.asList("java"), Arrays.asList("MONDAY_8AM-10AM"));

        AffinityScore score = calculator.calculate(a, b);

        double expected = 0.40 * score.getInterestScore()
                        + 0.30 * score.getAcademicScore()
                        + 0.30 * score.getScheduleScore();
        assertThat(score.getTotalScore()).isEqualTo(Math.round(expected * 10000.0) / 10000.0);
    }

    @Test
    void calculate_tagsWithWhitespace_normalizedCorrectly() {
        MatchProfile a = buildProfile(UUID.randomUUID(), "Engineering", 3,
                Arrays.asList("  java  ", " spring "), Arrays.asList("MONDAY_8AM-10AM"));
        MatchProfile b = buildProfile(UUID.randomUUID(), "Engineering", 3,
                Arrays.asList("java", "spring"), Arrays.asList("MONDAY_8AM-10AM"));

        AffinityScore score = calculator.calculate(a, b);

        assertThat(score.getInterestScore()).isEqualTo(1.0);
    }

    @Test
    void calculate_tagsWithBlankStrings_areIgnored() {
        MatchProfile a = buildProfile(UUID.randomUUID(), "Engineering", 3,
                Arrays.asList("java", "", "  "), Arrays.asList("MONDAY_8AM-10AM"));
        MatchProfile b = buildProfile(UUID.randomUUID(), "Engineering", 3,
                Arrays.asList("java"), Arrays.asList("MONDAY_8AM-10AM"));

        AffinityScore score = calculator.calculate(a, b);

        assertThat(score.getInterestScore()).isEqualTo(1.0);
    }
}
