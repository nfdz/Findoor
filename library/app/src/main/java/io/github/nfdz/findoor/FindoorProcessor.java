package io.github.nfdz.findoor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.nfdz.findoor.model.Record;
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

}
