package io.github.nfdz.findoordemoapp.compare;

import android.support.annotation.Nullable;

import io.github.nfdz.findoor.model.LocationComparison;

public interface CompareContract {

    interface View {
        void showLoading();
        void hideLoading();
        void showInvalidLocationAErrorMsg();
        void showInvalidLocationBErrorMsg();
        void showProcessErrorMsg();
        void setComparisonData(LocationComparison comparison);
    }

    interface Presenter {
        void onCreate();
        void onDestroy();
        void onCompareLocationClick(@Nullable Integer locationA, @Nullable Integer locationB);
    }

    interface Interactor {
        void initialize();
        void destroy();

        interface CompareCallback {
            void onSuccess(LocationComparison comparison);
            void onError();
        }
        void compareLocation(int locationA, int locationB, CompareCallback callback);
    }

}
