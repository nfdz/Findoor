package io.github.nfdz.findoordemoapp.view;

import android.support.annotation.Nullable;

import java.util.List;

import io.github.nfdz.findoor.model.RecordsStatistics;

public interface ViewContract {

    interface View {
        void setLocation(int location);
        void lockLocation();
        void unlockLocation();

        void setRecordsInfo(int samplesTotal, long firstRecordTimestamp, long lastRecordTimestamp);
        void clearRecordsInfo();
        void lockViewRecordsInterval();
        void unlockViewRecordsInterval();
        void setIntervalViewFrom(long intervalFrom);
        void setIntervalViewTo(long intervalTo);

        void showRecords(@Nullable List<RecordsStatistics> records);

        void showInvalidLocationErrorMsg();
        void showInvalidIntervalErrorMsg();
        void showProcessingError();
    }

    interface Presenter {
        void onCreate();
        void onDestroy();
        void onLoadLocationClick(@Nullable Integer location);
        void onCloseLocationClick();
        void onSetViewRecordsIntervalFrom(long fromTimestamp);
        void onSetViewRecordsIntervalTo(long toTimestamp);
        void onLoadIntervalRecordsClick();
        void onCloseIntervalRecordsClick();
    }

    interface Interactor {
        void initialize();
        void destroy();
        int getLocation();

        interface LoadLocationCallback {
            void onSuccess(int samplesTotal, long firstRecordTimestamp, long lastRecordTimestamp);
        }
        void loadLocation(int location, LoadLocationCallback callback);

        void setIntervalViewFrom(long fromTimestamp);
        void setIntervalViewTo(long toTimestamp);
        long getIntervalViewFrom();
        long getIntervalViewTo();

        interface LoadRecordsCallback {
            void onSuccess(List<RecordsStatistics> records);
            void onIntervalError();
            void onProcessingError();
        }
        void loadRecords(LoadRecordsCallback callback);
    }

}
