package io.github.nfdz.findoordemoapp.delete;

import android.support.annotation.Nullable;

public interface DeleteContract {

    interface View {

        void showDeleteSuccessMsg();
        void showDeleteErrorMsg();
        void showInvalidSamplesErrorMsg();
        void showInvalidLocationErrorMsg();
        void showInvalidIntervalErrorMsg();

        interface ConfirmationCallback {
            void onConfirm();
        }
        void askConfirmation(ConfirmationCallback callback);
    }

    interface Presenter {
        void onCreate();
        void onDestroy();

        void onDeleteLocation(@Nullable Integer location);
        void onDeleteIntervalClick(@Nullable Integer location, @Nullable Long fromTimestamp, @Nullable Long toTimestamp);
        void onDeleteWithLessSamplesClick(@Nullable Integer location, @Nullable Integer samples);
        void onDeleteAllClick();
    }

    interface Interactor {
        void initialize();
        void destroy();

        interface DeleteCallback {
            void onSuccess();
            void onError();
        }
        void deleteLocation(int location, DeleteCallback callback);
        void deleteLocationInterval(int location, long fromTimestamp, long toTimestamp, DeleteCallback callback);
        void deleteLocationWithLessSamples(int location, int samples, final DeleteCallback callback);
        void deleteAll(DeleteCallback callback);
    }

}
