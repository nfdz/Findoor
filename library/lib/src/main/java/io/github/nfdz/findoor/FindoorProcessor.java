package io.github.nfdz.findoor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.nfdz.findoor.model.LocationComparison;
import io.github.nfdz.findoor.model.Record;
import io.github.nfdz.findoor.model.RecordsComparison;
import io.github.nfdz.findoor.model.RecordsStatistics;
import io.github.nfdz.findoor.utils.StatisticsUtils;

public class FindoorProcessor {

    public static List<RecordsStatistics> computeStatistics(List<Record> records) {
        List<RecordsStatistics> statistics = new ArrayList<>();
        if (records == null || records.isEmpty()) {
            return statistics;
        }
        // Map BSSIDs
        Map<String,List<Record>> map = new HashMap<>();
        for (Record record : records) {
            List<Record> bssidRecords = map.get(record.bssid);
            if (bssidRecords == null) {
                bssidRecords = new ArrayList<>();
                map.put(record.bssid, bssidRecords);
            }
            bssidRecords.add(record);
        }
        // Process each BSSID
        for (Map.Entry<String,List<Record>> entry : map.entrySet()) {
            String bssid = entry.getKey();
            List<Record> samples = entry.getValue();
            int samplesSize = samples.size();
            int frequency = -1;
            String ssid = null;
            List<Integer> levels = new ArrayList<>();
            List<Integer> rssis = new ArrayList<>();
            for (Record record : samples) {
                if (ssid == null) {
                    ssid = record.ssid;
                }
                if (frequency == -1) {
                    frequency = record.frequency;
                }
                levels.add(record.level);
                rssis.add(record.rssi);
            }
            double realLevelMean = StatisticsUtils.calculateAverage(levels);
            int levelMean = (int) realLevelMean;
            int levelSd = (int) StatisticsUtils.calculateStandardDeviation(levels, realLevelMean);

            double realRssiMean = StatisticsUtils.calculateAverage(rssis);
            int rssiMean = (int) realRssiMean;
            int rssiSd = (int) StatisticsUtils.calculateStandardDeviation(rssis, realRssiMean);
            statistics.add(new RecordsStatistics(ssid, bssid, levelMean, levelSd, rssiMean, rssiSd, frequency, samplesSize));
        }
        // Sort by level mean
        Collections.sort(statistics, Collections.reverseOrder(new Comparator<RecordsStatistics>() {
            @Override
            public int compare(RecordsStatistics o1, RecordsStatistics o2) {
                return (o1.levelMean < o2.levelMean) ? -1 : ((o1.levelMean == o2.levelMean) ? 0 : 1);
            }
        }));
        return statistics;
    }

    public static LocationComparison computeComparisons(int locationToTestId,
                                                        List<RecordsStatistics> statisticsTarget,
                                                        List<RecordsStatistics> statisticsToTest) {
        List<RecordsComparison> comparisons = computeWifiComparisons(statisticsTarget, statisticsToTest);

        // Total Raw and Net (weighted arithmetic mean)
        double sumRaw = 0;
        double sumNet = 0;
        double weights = 0;
        for (RecordsComparison comparison : comparisons) {
            int weight = comparison.statisticsTarget.levelMean;
            sumRaw += (comparison.rawGap * weight);
            sumNet += (comparison.netGap * weight);
            weights += weight;
        }
        int rawGapMean = (int) (sumRaw/weights);
        int netGapMean = (int) (sumNet/weights);

        // Ignoring shutdowns
        sumRaw = 0;
        sumNet = 0;
        weights = 0;
        for (RecordsComparison comparison : comparisons) {
            if (comparison.statisticsTest == null) continue;
            int weight = comparison.statisticsTarget.levelMean;
            sumRaw += (comparison.rawGap * weight);
            sumNet += (comparison.netGap * weight);
            weights += weight;
        }
        int ignoringRawGapMean = (int) (sumRaw/weights);
        int ignoringNetGapMean = (int) (sumNet/weights);

        // Compute similarity
        int similarity = (int) (100f * computeSimilarity(statisticsTarget, statisticsToTest));
        return new LocationComparison(locationToTestId,
                rawGapMean,
                netGapMean,
                ignoringRawGapMean,
                ignoringNetGapMean,
                similarity,
                comparisons);
    }

    public static List<RecordsComparison> computeWifiComparisons(List<RecordsStatistics> statisticsTarget,
                                                                 List<RecordsStatistics> statisticsToTest) {
        List<RecordsComparison> result = new ArrayList<>();
        // Compute raw to net delta
        int maxTarget = getMaxLevel(statisticsTarget);
        int deltaTarget = 100 - maxTarget;
        int maxTest = getMaxLevel(statisticsToTest);
        int deltaTest = 100 - maxTest;
        // Map by BSSID
        Map<String,RecordsStatistics> mapTarget = new HashMap<>();
        for (RecordsStatistics statsTarget : statisticsTarget) {
            mapTarget.put(statsTarget.bssid, statsTarget);
        }
        Map<String,RecordsStatistics> mapTest = new HashMap<>();
        for (RecordsStatistics statsTest : statisticsToTest) {
            mapTest.put(statsTest.bssid, statsTest);
        }

        // Compute source location with destination location
        for (Map.Entry<String,RecordsStatistics> entryTarget : mapTarget.entrySet()) {
            String bssid = entryTarget.getKey();
            RecordsStatistics statsTarget = entryTarget.getValue();
            RecordsStatistics statsTest = mapTest.get(bssid);
            boolean onlyTarget = statsTest == null;
            int rawGap;
            int netGap;
            if (onlyTarget) {
                rawGap = statsTarget.levelMean;
                netGap = statsTarget.levelMean + deltaTarget;
            } else {
                rawGap = Math.abs(statsTarget.levelMean - statsTest.levelMean);
                netGap = Math.abs((statsTarget.levelMean + deltaTarget) - (statsTest.levelMean + deltaTest));
            }
            result.add(new RecordsComparison(statsTarget.ssid, bssid, rawGap, netGap, statsTarget, statsTest));
        }

        Collections.sort(result, new Comparator<RecordsComparison>() {
            @Override
            public int compare(RecordsComparison o1, RecordsComparison o2) {
                int o1Level = o1.statisticsTarget.levelMean;
                int o2Level = o2.statisticsTarget.levelMean;
                return (o1Level < o2Level) ? 1 : ((o1Level == o2Level) ? 0 : -1);
            }
        });
        return result;
    }

    public static int getMaxLevel(List<RecordsStatistics> statistics) {
        int max = 0;
        for (RecordsStatistics stats : statistics) {
            if (stats.levelMean > max) max = stats.levelMean;
        }
        return max;
    }

    private static Set<String> getBSSIDs(List<RecordsStatistics> statistics) {
        Set<String> bssids = new HashSet<>();
        for (RecordsStatistics stats : statistics) {
            bssids.add(stats.bssid);
        }
        return bssids;
    }

    private static double computeSimilarity(List<RecordsStatistics> statisticsTarget, List<RecordsStatistics> statisticsTest) {
        double sum = 0;
        double weights = 0;
        Set<String> bssidsTest = getBSSIDs(statisticsTest);
        for (RecordsStatistics statsTarget : statisticsTarget) {
            int weight = statsTarget.levelMean;
            weights += weight;
            if (bssidsTest.contains(statsTarget.bssid)) {
                sum += weight;
            }
        }
        return sum/weights;
    }

    public static List<RecordsStatistics> filterByMinSamples(List<RecordsStatistics> statistics, int minSamples) {
        if (minSamples <= 0) return statistics;
        List<RecordsStatistics> result = new ArrayList<>();
        for (RecordsStatistics stats : statistics) {
            if (stats.samples >= minSamples) result.add(stats);
        }
        return result;
    }

}
