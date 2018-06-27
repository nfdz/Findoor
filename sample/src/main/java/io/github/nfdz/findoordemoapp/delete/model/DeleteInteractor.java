package io.github.nfdz.findoordemoapp.delete.model;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import io.github.nfdz.findoor.FindoorProcessor;
import io.github.nfdz.findoor.model.Record;
import io.github.nfdz.findoor.model.RecordsStatistics;
import io.github.nfdz.findoordemoapp.common.model.WifiRecord;
import io.github.nfdz.findoordemoapp.common.utils.PreferencesUtils;
import io.github.nfdz.findoordemoapp.common.utils.RealmUtils;
import io.github.nfdz.findoordemoapp.delete.DeleteContract;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class DeleteInteractor implements DeleteContract.Interactor {

    private Realm realm;
    private Context context;

    public DeleteInteractor(Context context) {
        this.context = context;
    }

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
        context = null;
    }

    @Override
    public void deleteLocation(final int location, final DeleteCallback callback) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                realm.where(WifiRecord.class).equalTo(WifiRecord.LOCATION_FIELD, location).findAll().deleteAllFromRealm();
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
                if (context != null) {
                    PreferencesUtils.setLocationAlias(context, location, "");
                }
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(@NonNull Throwable error) {
                callback.onError();
            }
        });
    }

    @Override
    public void deleteLocationInterval(final int location, final long fromTimestamp, final long toTimestamp, final DeleteCallback callback) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                realm.where(WifiRecord.class)
                        .equalTo(WifiRecord.LOCATION_FIELD, location)
                        .greaterThanOrEqualTo(WifiRecord.TIMESTAMP_FIELD, fromTimestamp)
                        .lessThanOrEqualTo(WifiRecord.TIMESTAMP_FIELD, toTimestamp)
                        .findAll()
                        .deleteAllFromRealm();
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(@NonNull Throwable error) {
                callback.onError();
            }
        });
    }

    @Override
    public void deleteLocationWithLessSamples(final int location, final int samples, final DeleteCallback callback) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                // Compute statistics
                RealmResults<WifiRecord> result = realm.where(WifiRecord.class)
                        .equalTo(WifiRecord.LOCATION_FIELD, location)
                        .findAll();
                List<Record> records = new ArrayList<>();
                for (WifiRecord wifiRecord : result) {
                    records.add(wifiRecord.toRecord());
                }
                List<RecordsStatistics> statistics = FindoorProcessor.computeStatistics(records);

                // Query bssids with less samples
                final Set<String> bssidsToDelete = new HashSet<>();
                for (RecordsStatistics stats : statistics) {
                    if (stats.samples < samples) {
                        bssidsToDelete.add(stats.bssid);
                    }
                }
                if (bssidsToDelete.isEmpty()) {
                    callback.onSuccess();
                } else {
                    RealmQuery<WifiRecord> query = realm.where(WifiRecord.class);
                    Iterator<String> it = bssidsToDelete.iterator();
                    while (it.hasNext()) {
                        query.equalTo(WifiRecord.BSSID_FIELD, it.next());
                        if (it.hasNext()) {
                            query.or();
                        }
                    }
                    query.findAll().deleteAllFromRealm();
                }

            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(@NonNull Throwable error) {
                callback.onError();
            }
        });
    }

    @Override
    public void deleteAll(final DeleteCallback callback) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                realm.delete(WifiRecord.class);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
                if (context != null) {
                    PreferencesUtils.clearLocationAlias(context);
                }
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(@NonNull Throwable error) {
                callback.onError();
            }
        });
    }

}
