package io.github.nfdz.findoordemoapp.trial.model;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import io.github.nfdz.findoor.FindoorProcessor;
import io.github.nfdz.findoor.FindoorRecorder;
import io.github.nfdz.findoor.model.DisabledWifiException;
import io.github.nfdz.findoor.model.LocationComparison;
import io.github.nfdz.findoor.model.Record;
import io.github.nfdz.findoor.model.RecordsStatistics;
import io.github.nfdz.findoordemoapp.common.model.WifiRecord;
import io.github.nfdz.findoordemoapp.common.utils.RealmUtils;
import io.github.nfdz.findoordemoapp.trial.TrialContract;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class TrialInteractor implements TrialContract.Interactor, FindoorRecorder.Listener {

    private static final int MIN_SAMPLES = 3;

    private final Context context;
    private final FindoorRecorder recorder;
    private final List<Integer> locationsToCompare;
    private final List<Record> records;
    private final AtomicReference<TrialProcessListener> listenerRef;

    private Realm realm;


    public TrialInteractor(Context context) {
        this.context = context;
        this.recorder = new FindoorRecorder(context);
        this.recorder.setListener(this);
        locationsToCompare = new CopyOnWriteArrayList<>();
        records = new CopyOnWriteArrayList<>();
        listenerRef = new AtomicReference<>(null);
    }

    @Override
    public void initialize() {
        realm = Realm.getInstance(RealmUtils.getConfiguration());
    }

    @Override
    public void destroy() {
        recorder.stopRecord();
        if (realm != null) {
            realm.close();
            realm = null;
        }
    }

    @Override
    public void getAllLocations(final GetAllLocationsCallback callback) {
        RealmUtils.listLocations(realm, new RealmUtils.ListLocationsCallback() {
            @Override
            public void onFinish(List<Integer> locations) {
                callback.onFinish(locations);
            }
            @Override
            public void onError() {
                callback.onError();
            }
        });
    }

    @Override
    public void setLocations(List<Integer> locations) {
        locationsToCompare.clear();
        locationsToCompare.addAll(locations);
    }

    @Override
    public void startTrial(TrialProcessListener listener) {
        records.clear();
        listenerRef.set(listener);
        try {
            recorder.startRecord();
        } catch (DisabledWifiException e) {
            listener.onWifiDisabledError();
        }
    }

    @Override
    public void stopTrial() {
        recorder.stopRecord();
        listenerRef.set(null);
    }

    @Override
    public void onNotifyRecords(List<Record> newRecords, long recordTime) {
        records.addAll(newRecords);
        ProcessComparisonsTask task = new ProcessComparisonsTask(records, locationsToCompare, listenerRef);
        realm.executeTransactionAsync(task, task);
    }

    private static class ProcessComparisonsTask implements Realm.Transaction, Realm.Transaction.OnSuccess {

        private List<LocationComparison> result;
        private List<Record> records;
        private List<Integer> locationsToCompare;
        private AtomicReference<TrialProcessListener> listenerRef;

        public ProcessComparisonsTask(List<Record> records,
                                      List<Integer> locationsToCompare,
                                      AtomicReference<TrialProcessListener> listenerRef) {
            this.records = new ArrayList<>(records);
            this.locationsToCompare = new ArrayList<>(locationsToCompare);
            this.listenerRef = listenerRef;
        }

        @Override
        public void execute(@NonNull Realm realm) {
            result = new ArrayList<>();
            List<RecordsStatistics> statisticsTarget = FindoorProcessor.computeStatistics(records);
            statisticsTarget = FindoorProcessor.filterByMinSamples(statisticsTarget, MIN_SAMPLES);

            for (int locationToTest : locationsToCompare) {
                RealmResults<WifiRecord> wifiRecordsToTest = realm.where(WifiRecord.class)
                        .equalTo(WifiRecord.LOCATION_FIELD, locationToTest)
                        .sort(WifiRecord.TIMESTAMP_FIELD, Sort.ASCENDING)
                        .findAll();
                List<Record> recordsToTest = new ArrayList<>();
                for (WifiRecord wifiRecord : wifiRecordsToTest) {
                    recordsToTest.add(wifiRecord.toRecord());
                }
                List<RecordsStatistics> statisticsToTest = FindoorProcessor.computeStatistics(recordsToTest);
                statisticsToTest = FindoorProcessor.filterByMinSamples(statisticsToTest, MIN_SAMPLES);
                result.add(FindoorProcessor.computeComparisons(locationToTest, statisticsTarget, statisticsToTest));
            }

            Collections.sort(result, new Comparator<LocationComparison>() {
                @Override
                public int compare(LocationComparison o1, LocationComparison o2) {
                    return (o1.netGapMean < o2.netGapMean) ? -1 : ((o1.netGapMean == o2.netGapMean) ? 0 : +1);
                }
            });
        }

        @Override
        public void onSuccess() {
            TrialProcessListener listener = listenerRef.get();
            if (result != null && listener != null) {
                listener.onNotifyResult(result, records.size());
            }
        }

    }
}
