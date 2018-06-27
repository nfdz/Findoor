package io.github.nfdz.findoordemoapp.common.utils;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.nfdz.findoordemoapp.common.model.WifiRecord;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;

public class RealmUtils {

    private static final String DB_NAME = "findoor_demo.realm";
    private static final long SCHEMA_VERSION_NAME = 1;

    public static RealmConfiguration getConfiguration() {
        return new RealmConfiguration.Builder()
                .name(DB_NAME)
                .schemaVersion(SCHEMA_VERSION_NAME)
                .deleteRealmIfMigrationNeeded()
                .build();
    }

    public interface ListLocationsCallback {
        void onFinish(List<Integer> locations);
        void onError();
    }

    public static void listLocations(Realm realm, final ListLocationsCallback callback) {
        final List<Integer> locations = new ArrayList<>();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                WifiRecord result = realm.where(WifiRecord.class).findFirst();
                if (result == null) return;
                locations.add(result.location);

                Integer newLocation = null;
                do {
                    RealmQuery<WifiRecord> query = realm.where(WifiRecord.class);
                    for (Integer location : locations) {
                        query.notEqualTo(WifiRecord.LOCATION_FIELD, location);
                    }
                    result = query.findFirst();
                    if (result != null) {
                        newLocation = result.location;
                        locations.add(newLocation);
                    } else {
                        newLocation = null;
                    }
                } while(newLocation != null);
                Collections.sort(locations);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                callback.onFinish(locations);
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(@NonNull Throwable error) {
                callback.onError();
            }
        });
    }

}
