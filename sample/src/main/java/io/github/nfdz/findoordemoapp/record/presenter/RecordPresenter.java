package io.github.nfdz.findoordemoapp.record.presenter;

import android.content.Context;
import android.support.annotation.Nullable;

import java.util.List;

import io.github.nfdz.findoordemoapp.common.model.WifiRecord;
import io.github.nfdz.findoordemoapp.record.RecordContract;
import io.github.nfdz.findoordemoapp.record.model.RecordInteractor;

public class RecordPresenter implements RecordContract.Presenter, RecordContract.Interactor.RecordProcessListener {

    private RecordContract.View view;
    private RecordContract.Interactor interactor;

    public RecordPresenter(RecordContract.View view, Context context) {
        this.view = view;
        interactor = new RecordInteractor(context);
    }

    @Override
    public void onCreate() {
        interactor.initialize();
        view.setLocation(interactor.getLocation());
        view.showStartButton(true);
        view.showStopButton(false);
        view.enableWifiService();
        view.askLocationService();
    }

    @Override
    public void onDestroy() {
        if (interactor != null) interactor.destroy();
        interactor = null;
        view = null;
    }

    @Override
    public void onStartRecordClick(@Nullable Integer location) {
        if (location != null) {
            interactor.setLocation(location);
            view.askPermissions();
        } else {
            view.showInvalidLocationErrorMsg();
        }
    }

    @Override
    public void onStopRecordClick() {
        interactor.stopRecord();
        view.showRecords(null, 0);
        view.unlockLocation();

        view.showStartButton(true);
        view.showStopButton(false);
    }

    @Override
    public void onPermissionsGranted() {
        // Continue with start record process
        startRecord();
    }

    private void startRecord() {
        view.lockLocation();
        view.showStartButton(false);
        view.showStopButton(true);
        interactor.startRecord(this);
    }

    @Override
    public void onPermissionsDenied() {
        view.showPermissionsErrorMsg();
        view.navigateToBack();
    }

    @Override
    public void onWifiDisabledError() {
        if (view != null && interactor != null) {
            view.showWifiDisabledErrorMsg();
            onStopRecordClick();
        }
    }

    @Override
    public void onNotifyRecords(List<WifiRecord> records, long recordTime) {
        if (view != null) {
            view.showRecords(records, recordTime);
        }
    }
}