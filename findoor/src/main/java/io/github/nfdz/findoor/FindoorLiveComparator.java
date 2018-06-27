package io.github.nfdz.findoor;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import io.github.nfdz.findoor.model.DisabledWifiException;
import io.github.nfdz.findoor.model.LocationComparison;
import io.github.nfdz.findoor.model.Record;
import io.github.nfdz.findoor.model.RecordsStatistics;

/**
 * Live comparator class.
 */
public class FindoorLiveComparator {

    /**
     * Listener interface to be implemented.
     */
    public interface Listener {
        /**
         * This method notifies when new comparisons were computed.
         */
        void onNotifyComparisons(List<LocationComparison> comparisons, int samples);
    }

    private final List<LocationComparison> comparisons;
    private final List<Record> allRecords;
    private final FindoorRecorder recorder;
    private final Map<Integer,List<Record>> locationRecordsMap;

    private Map<Integer,List<RecordsStatistics>> locationStatisticsCache;
    private Listener listener;

    public FindoorLiveComparator(Context context, Map<Integer,List<Record>> locationRecordsMap) {
        this.comparisons = new ArrayList<>();
        this.allRecords = new CopyOnWriteArrayList<>();
        this.recorder = new FindoorRecorder(context);
        this.recorder.setListener(new RecorderListener());
        this.locationRecordsMap = locationRecordsMap;
        this.locationStatisticsCache = null;
    }

    /**
     * Set a custom scan frequency.
     */
    public void setScanFrequency(long scanFrequencyMillis) {
        recorder.setScanFrequency(scanFrequencyMillis);
    }

    /**
     * Set listener.
     */
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /**
     * Clear recorded samples.
     */
    public void clear() {
        allRecords.clear();
        comparisons.clear();
    }

    /**
     * Get last list of computed comparisons.
     */
    public List<LocationComparison> getComparisons() {
        return Collections.unmodifiableList(comparisons);
    }

    /**
     * Get amount of samples recorded.
     */
    public int getSamplesCount() {
        return allRecords.size();
    }

    /**
     * Start scan and compute process.
     * @throws DisabledWifiException
     */
    public void startRecord() throws DisabledWifiException {
        recorder.startRecord();
    }

    /**
     * Stop scan and compute process.
     */
    public void stopRecord() {
        recorder.stopRecord();
    }

    private class RecorderListener implements FindoorRecorder.Listener {
        @Override
        public void onNotifyRecords(List<Record> records, long recordTime) {
            allRecords.addAll(records);
            ProcessRecordsTask.execute(locationRecordsMap, allRecords, locationStatisticsCache, new ProcessCallback());
        }
    }

    private class ProcessCallback implements ProcessRecordsTask.Callback {
        @Override
        public void onProcessFinish(List<LocationComparison> newComparisons,
                                    Map<Integer, List<RecordsStatistics>> cache) {
            if (locationStatisticsCache == null) {
                locationStatisticsCache = cache;
            }
            comparisons.clear();
            comparisons.addAll(newComparisons);
            if (listener != null) {
                listener.onNotifyComparisons(newComparisons, allRecords.size());
            }
        }
    }

    private static class ProcessRecordsTask extends AsyncTask<Void,Void,Void> {

        public static void execute(Map<Integer,List<Record>> locationRecords,
                                   List<Record> records,
                                   Map<Integer,List<RecordsStatistics>> locationStatisticsCache,
                                   Callback callback) {

        }

        public interface Callback {
            void onProcessFinish(List<LocationComparison> comparisons,
                                 Map<Integer,List<RecordsStatistics>> locationStatisticsCache);
        }

        private final List<LocationComparison> result;
        private final Map<Integer,List<Record>> locationRecords;
        private final List<Record> records;
        private final Callback callback;
        private Map<Integer,List<RecordsStatistics>> locationStatisticsCache;

        public ProcessRecordsTask(Map<Integer,List<Record>> locationRecords,
                                  List<Record> records,
                                  Map<Integer,List<RecordsStatistics>> locationStatisticsCache,
                                  Callback callback) {
            this.locationRecords = locationRecords;
            this.records = records;
            this.callback = callback;
            this.locationStatisticsCache = locationStatisticsCache;
            this.result = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Process cache if needed
            if (locationStatisticsCache == null) {
                locationStatisticsCache = new ConcurrentHashMap<>();
                for (Map.Entry<Integer,List<Record>> entry : locationRecords.entrySet()) {
                    List<RecordsStatistics> statistics = FindoorProcessor.computeStatistics(entry.getValue());
                    locationStatisticsCache.put(entry.getKey(), statistics);
                }
            }

            // Process comparisons
            List<RecordsStatistics> statistics = FindoorProcessor.computeStatistics(records);
            for (Map.Entry<Integer,List<RecordsStatistics>> entry : locationStatisticsCache.entrySet()) {
                result.add(FindoorProcessor.computeLocationComparisons(entry.getKey(), statistics, entry.getValue()));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (callback != null) {
                callback.onProcessFinish(result, locationStatisticsCache);
            }
        }
    }

}
