package io.github.nfdz.findoor.utils;

import java.util.List;

/**
 * Helper class with common statistics functions.
 */
public class StatisticsUtils {

    public static double calculateAverage(List<Integer> marks) {
        if (marks == null || marks.isEmpty()) {
            return 0;
        }
        Integer sum = 0;
        for (Integer mark : marks) {
            sum += mark;
        }
        return sum.doubleValue() / marks.size();
    }

    public static double calculateStandardDeviation(List<Integer> marks) {
        return calculateStandardDeviation(marks, calculateAverage(marks));
    }

    public static double calculateStandardDeviation(List<Integer> marks, double mean) {
        if (marks == null || marks.isEmpty()) {
            return 0;
        }
        double temp = 0;
        for (Integer mark : marks) {
            double squrDiffToMean = Math.pow(mark - mean, 2);
            temp += squrDiffToMean;
        }
        double meanOfDiffs = (double) temp / (double) (marks.size());
        return Math.sqrt(meanOfDiffs);
    }

}
