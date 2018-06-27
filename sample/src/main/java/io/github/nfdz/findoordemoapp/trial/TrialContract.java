package io.github.nfdz.findoordemoapp.trial;

import android.support.annotation.Nullable;

import java.util.List;

import io.github.nfdz.findoor.model.LocationComparison;

public interface TrialContract {

    interface View {
        void setLocations(String locations);
        void setResult(List<LocationComparison> comparisons, int samples);

        void showStartButton(boolean state);
        void showStopButton(boolean state);
        void lockLocations();
        void unlockLocations();

        void showListLocationsErrorMsg();
        void showPermissionsErrorMsg();
        void showInvalidLocationsErrorMsg();
        void showWifiDisabledErrorMsg();
        void enableWifiService();
        void askLocationService();
        void askPermissions();
        void navigateToBack();
    }

    interface Presenter {
        void onCreate();
        void onDestroy();
        void onStartClick(@Nullable String locations);
        void onStopClick();
        void onPermissionsGranted();
        void onPermissionsDenied();
    }

    interface Interactor {
        void initialize();
        void destroy();

        interface GetAllLocationsCallback {
            void onFinish(List<Integer> locations);
            void onError();
        }
        void getAllLocations(GetAllLocationsCallback callback);
        void setLocations(List<Integer> locations);
        interface TrialProcessListener {
            void onWifiDisabledError();
            void onNotifyResult(List<LocationComparison> comparisons, int samples);
        }
        void startTrial(TrialProcessListener listener);
        void stopTrial();
    }

}
