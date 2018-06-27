package io.github.nfdz.findoordemoapp.view.model;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.github.nfdz.findoor.FindoorProcessor;
import io.github.nfdz.findoor.model.Record;
import io.github.nfdz.findoor.model.RecordsStatistics;
import io.github.nfdz.findoordemoapp.common.model.WifiRecord;
import io.github.nfdz.findoordemoapp.common.utils.PreferencesUtils;
import io.github.nfdz.findoordemoapp.common.utils.RealmUtils;
import io.github.nfdz.findoordemoapp.view.ViewContract;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class ViewInteractor implements ViewContract.Interactor {

    private Context context;
    private Realm realm;

    private int location;
    private long fromTimestamp;
    private long toTimestamp;

    public ViewInteractor(Context context) {
        this.context = context;
    }

    @Override
    public void initialize() {
        location = PreferencesUtils.getLastLocationCache(context);
        realm = Realm.getInstance(RealmUtils.getConfiguration());
    }

    @Override
    public void destroy() {
        if (realm != null) {
            realm.close();
            realm = null;
        }
        context = null;
    }

    @Override
    public int getLocation() {
        return location;
    }

    @Override
    public void loadLocation(int location, LoadLocationCallback callback) {
        this.location = location;
        PreferencesUtils.setLastLocationCache(context, location);
        RealmResults<WifiRecord> result = realm.where(WifiRecord.class)
                .equalTo(WifiRecord.LOCATION_FIELD, location)
                .sort(WifiRecord.TIMESTAMP_FIELD, Sort.ASCENDING)
                .findAll();
        int samplesTotal = result.size();
        long firstRecordTimestamp = 0;
        long lastRecordTimestamp = 0;
        if (samplesTotal > 0) {
            firstRecordTimestamp = result.get(0).timestamp;
            lastRecordTimestamp = result.get(samplesTotal - 1).timestamp;
        }
        fromTimestamp = firstRecordTimestamp;
        toTimestamp = lastRecordTimestamp;
        callback.onSuccess(samplesTotal, fromTimestamp, toTimestamp);
    }

    @Override
    public void setIntervalViewFrom(long fromTimestamp) {
        this.fromTimestamp = fromTimestamp;
    }

    @Override
    public void setIntervalViewTo(long toTimestamp) {
        this.toTimestamp = toTimestamp;
    }

    @Override
    public long getIntervalViewFrom() {
        return fromTimestamp;
    }

    @Override
    public long getIntervalViewTo() {
        return toTimestamp;
    }

    @Override
    public void loadRecords(final LoadRecordsCallback callback) {
        final long intervalFromToLoad = fromTimestamp;
        final long intervalToToLoad = toTimestamp;
        if (fromTimestamp < 0 || fromTimestamp > toTimestamp) {
            callback.onIntervalError();
            return;
        }
        final int locationToLoad = location;
        final List<RecordsStatistics> statistics = new ArrayList<>();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                RealmResults<WifiRecord> result = realm.where(WifiRecord.class)
                        .equalTo(WifiRecord.LOCATION_FIELD, locationToLoad)
                        .greaterThanOrEqualTo(WifiRecord.TIMESTAMP_FIELD, intervalFromToLoad)
                        .lessThanOrEqualTo(WifiRecord.TIMESTAMP_FIELD, intervalToToLoad)
                        .sort(WifiRecord.TIMESTAMP_FIELD, Sort.ASCENDING)
                        .findAll();
                List<Record> records = new ArrayList<>();
                for (WifiRecord wifiRecord : result) {
                    records.add(wifiRecord.toRecord());
                }
                statistics.addAll(FindoorProcessor.computeStatistics(records));
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                if (locationToLoad == location && intervalFromToLoad == fromTimestamp && intervalToToLoad == toTimestamp) {
                    callback.onSuccess(statistics);
                }
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(@NonNull Throwable error) {
                if (locationToLoad == location && intervalFromToLoad == fromTimestamp && intervalToToLoad == toTimestamp) {
                    callback.onProcessingError();
                }
            }
        });
    }

}
