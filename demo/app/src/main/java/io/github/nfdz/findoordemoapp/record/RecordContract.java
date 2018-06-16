package io.github.nfdz.findoordemoapp.record;

import android.support.annotation.Nullable;

import java.util.List;

import io.github.nfdz.findoordemoapp.common.model.WifiRecord;

public interface RecordContract {

    interface View {
        void setLocation(int location);
        void lockLocation();
        void unlockLocation();
        void showStartButton(boolean state);
        void showStopButton(boolean state);
        void showRecords(@Nullable List<WifiRecord> records, long recordTime);
        void showPermissionsErrorMsg();
        void showInvalidLocationErrorMsg();
        void showWifiDisabledErrorMsg();
        void enableWifiService();
        void askLocationService();
        void askPermissions();
        void navigateToBack();
    }

    interface Presenter {
        void onCreate();
        void onDestroy();
        void onStartRecordClick(@Nullable Integer location);
        void onStopRecordClick();
        void onPermissionsGranted();
        void onPermissionsDenied();
    }

    interface Interactor {
        void initialize();
        void destroy();
        void setLocation(int location);
        int getLocation();

        interface RecordProcessListener {
            void onWifiDisabledError();
            void onNotifyRecords(List<WifiRecord> records, long recordTime);
        }
        void startRecord(@Nullable RecordProcessListener listener);
        void stopRecord();
    }

}
