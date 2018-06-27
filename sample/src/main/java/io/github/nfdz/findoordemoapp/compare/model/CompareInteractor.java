package io.github.nfdz.findoordemoapp.compare.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.github.nfdz.findoor.FindoorProcessor;
import io.github.nfdz.findoor.model.LocationComparison;
import io.github.nfdz.findoor.model.Record;
import io.github.nfdz.findoor.model.RecordsStatistics;
import io.github.nfdz.findoordemoapp.common.model.WifiRecord;
import io.github.nfdz.findoordemoapp.common.utils.RealmUtils;
import io.github.nfdz.findoordemoapp.compare.CompareContract;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class CompareInteractor implements CompareContract.Interactor {

    // Strongly related with record frequency
    private static final int MIN_SAMPLES_TO_COMPARE = 3;

    private Realm realm;

    @Override
    public void initialize() {
        realm = Realm.getInstance(RealmUtils.getConfiguration());
    }

    @Override
    public void destroy() {
        if (realm != null) {
            realm.close();
            realm = null;
        }
    }

    @Override
    public void compareLocation(final int locationA, final int locationB, final CompareCallback callback) {
        final AtomicReference<LocationComparison> result = new AtomicReference<>(null);
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                // Location target
                RealmResults<WifiRecord> resultA = realm.where(WifiRecord.class)
                        .equalTo(WifiRecord.LOCATION_FIELD, locationA)
                        .sort(WifiRecord.TIMESTAMP_FIELD, Sort.ASCENDING)
                        .findAll();
                List<Record> recordsA = new ArrayList<>();
                for (WifiRecord wifiRecord : resultA) {
                    recordsA.add(wifiRecord.toRecord());
                }
                List<RecordsStatistics> statisticsA = FindoorProcessor.computeStatistics(recordsA);
                statisticsA = FindoorProcessor.filterByMinSamples(statisticsA, MIN_SAMPLES_TO_COMPARE);

                // Location to test
                RealmResults<WifiRecord> resultB = realm.where(WifiRecord.class)
                        .equalTo(WifiRecord.LOCATION_FIELD, locationB)
                        .sort(WifiRecord.TIMESTAMP_FIELD, Sort.ASCENDING)
                        .findAll();
                List<Record> recordsB = new ArrayList<>();
                for (WifiRecord wifiRecord : resultB) {
                    recordsB.add(wifiRecord.toRecord());
                }
                List<RecordsStatistics> statisticsB = FindoorProcessor.computeStatistics(recordsB);
                statisticsB = FindoorProcessor.filterByMinSamples(statisticsB, MIN_SAMPLES_TO_COMPARE);

                result.set(FindoorProcessor.computeLocationComparisons(locationB, statisticsA, statisticsB));
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                callback.onSuccess(result.get());
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(@NonNull Throwable error) {
                callback.onError();
            }
        });
    }

}
