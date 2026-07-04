package co.edu.escuelaing.alphaeci.matching_service.application.service;


import org.springframework.stereotype.Service;

import co.edu.escuelaing.alphaeci.matching_service.domain.model.MatchProfile;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.AffinityScore;

import java.util.*;

@Service
public class AffinityCalculatorImpl implements AffinityCalculator {

    private static final double W_INTEREST = 0.40;
    private static final double W_ACADEMIC = 0.30;
    private static final double W_SCHEDULE = 0.30;

    private static final double W_CAREER = 0.60;
    private static final double W_SEMESTER = 0.40;

    private static final double MAX_SEMESTER_DIFF = 9.0;

    @Override
    public AffinityScore calculate(MatchProfile a, MatchProfile b) {
        double interest = calculateInterestScore(a, b);
        double academic = calculateAcademicScore(a, b);
        double schedule = calculateScheduleScore(a, b);

        double total = W_INTEREST * interest
                     + W_ACADEMIC * academic
                     + W_SCHEDULE * schedule;

        AffinityScore result = new AffinityScore();
        result.setInterestScore(round(interest));
        result.setAcademicScore(round(academic));
        result.setScheduleScore(round(schedule));
        result.setTotalScore(round(total));

        return result;
    }

    // ---------------- INTEREST ----------------

    private double calculateInterestScore(MatchProfile a, MatchProfile b) {
        Set<String> tagsA = normalizeTags(a.getTags());
        Set<String> tagsB = normalizeTags(b.getTags());

        if (tagsA.isEmpty() && tagsB.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(tagsA);
        intersection.retainAll(tagsB);

        Set<String> union = new HashSet<>(tagsA);
        union.addAll(tagsB);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    // ---------------- ACADEMIC ----------------

    private double calculateAcademicScore(MatchProfile a, MatchProfile b) {
        double careerScore = sameNormalizedValue(a.getCareer(), b.getCareer()) ? 1.0 : 0.0;

        double semesterScore = 0.0;
        if (a.getSemester() != null && b.getSemester() != null) {
            double diff = Math.abs(a.getSemester() - b.getSemester());
            semesterScore = Math.max(0.0, 1.0 - (diff / MAX_SEMESTER_DIFF));
        }

        return W_CAREER * careerScore
             + W_SEMESTER * semesterScore;
    }

    // ---------------- SCHEDULE ----------------

    private double calculateScheduleScore(MatchProfile a, MatchProfile b) {
        Set<String> schedulesA = normalizeSchedules(a.getSchedulesAvailable());
        Set<String> schedulesB = normalizeSchedules(b.getSchedulesAvailable());

        if (schedulesA.isEmpty() || schedulesB.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(schedulesA);
        intersection.retainAll(schedulesB);

        Set<String> union = new HashSet<>(schedulesA);
        union.addAll(schedulesB);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    private Set<String> normalizeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> normalized = new HashSet<>();
        for (String tag : tags) {
            if (tag != null && !tag.isBlank()) {
                normalized.add(tag.trim().toLowerCase(Locale.ROOT));
            }
        }
        return normalized;
    }

    private Set<String> normalizeSchedules(List<String> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> normalized = new HashSet<>();
        for (String schedule : schedules) {
            if (schedule != null && !schedule.isBlank()) {
                normalized.add(schedule.trim().toLowerCase(Locale.ROOT));
            }
        }
        return normalized;
    }

    private boolean sameNormalizedValue(String left, String right) {
        if (left == null || right == null) {
            return false;
        }

        return left.trim().equalsIgnoreCase(right.trim());
    }

    private double round(double v) {
        return Math.round(v * 10000.0) / 10000.0;
    }
}
