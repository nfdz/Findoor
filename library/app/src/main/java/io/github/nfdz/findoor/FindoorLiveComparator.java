package io.github.nfdz.findoor;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import io.github.nfdz.findoor.model.DisabledWifiException;
import io.github.nfdz.findoor.model.LocationsComparison;
import io.github.nfdz.findoor.model.Record;
import io.github.nfdz.findoor.model.RecordsStatistics;

public class FindoorLiveComparator {

    public interface Listener {
        void onNotifyComparisons(List<LocationsComparison> comparisons, int samples);
    }

    private final List<LocationsComparison> comparisons;
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

    public void setScanFrecuency(long scanFrequencyMillis) {
        recorder.setScanFrecuency(scanFrequencyMillis);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void clear() {
        comparisons.clear();
    }

    public List<LocationsComparison> getComparisons() {
        return Collections.unmodifiableList(comparisons);
    }

    public int getSamplesCount() {
        return allRecords.size();
    }

    public void startRecord() throws DisabledWifiException {
        recorder.startRecord();
    }

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
        public void onProcessFinish(List<LocationsComparison> newComparisons, Map<Integer, List<RecordsStatistics>> locationStatisticsCache) {
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
            void onProcessFinish(List<LocationsComparison> comparisons,
                                 Map<Integer,List<RecordsStatistics>> locationStatisticsCache);
        }

        private final Map<Integer,List<Record>> locationRecords;
        private final List<Record> records;
        private final Map<Integer,List<RecordsStatistics>> locationStatisticsCache;
        private final Callback callback;

        public ProcessRecordsTask(Map<Integer,List<Record>> locationRecords,
                                  List<Record> records,
                                  Map<Integer,List<RecordsStatistics>> locationStatisticsCache,
                                  Callback callback) {
            this.locationRecords = locationRecords;
            this.records = records;
            this.locationStatisticsCache = locationStatisticsCache;
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // TODO
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // TODO
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // TODO
        }
    }

}
