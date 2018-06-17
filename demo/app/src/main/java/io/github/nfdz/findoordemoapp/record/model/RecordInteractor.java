package io.github.nfdz.findoordemoapp.record.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.nfdz.findoor.FindoorRecorder;
import io.github.nfdz.findoor.model.DisabledWifiException;
import io.github.nfdz.findoor.model.Record;
import io.github.nfdz.findoordemoapp.common.model.WifiRecord;
import io.github.nfdz.findoordemoapp.common.utils.PreferencesUtils;
import io.github.nfdz.findoordemoapp.common.utils.RealmUtils;
import io.github.nfdz.findoordemoapp.record.RecordContract;
import io.realm.Realm;

public class RecordInteractor implements RecordContract.Interactor, FindoorRecorder.Listener {

    private final Context context;
    private final FindoorRecorder recorder;

    private Realm realm;
    private int location;
    private RecordProcessListener listener;

    public RecordInteractor(Context context) {
        this.context = context;
        this.recorder = new FindoorRecorder(context);
        this.recorder.setListener(this);
    }

    @Override
    public void initialize() {
        location = PreferencesUtils.getLastLocationCache(context);
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
    public void setLocation(int location) {
        this.location = location;
        PreferencesUtils.setLastLocationCache(context, location);
    }

    @Override
    public int getLocation() {
        return location;
    }

    @Override
    public void startRecord(@Nullable RecordProcessListener listener) {
        this.listener = listener;
        try {
            recorder.startRecord();
        } catch (DisabledWifiException e) {
            if (listener != null) {
                listener.onWifiDisabledError();
            }
        }
    }

    @Override
    public void stopRecord() {
        recorder.stopRecord();
        listener = null;
    }

    @Override
    public void onNotifyRecords(List<Record> records, long recordTime) {
        final List<WifiRecord> result = new ArrayList<>();
        for (Record record : records) {
            result.add(WifiRecord.buildFromRecord(location, record));
        }
        List<WifiRecord> recordsToNotify = Collections.unmodifiableList(new ArrayList<>(result));
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                realm.copyToRealm(result);
            }
        });
        if (listener != null) {
            listener.onNotifyRecords(recordsToNotify, recordTime);
        }
    }

}
