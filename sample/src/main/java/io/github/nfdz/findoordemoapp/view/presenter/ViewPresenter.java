package io.github.nfdz.findoordemoapp.view.presenter;

import android.content.Context;
import android.support.annotation.Nullable;

import java.util.List;

import io.github.nfdz.findoor.model.RecordsStatistics;
import io.github.nfdz.findoordemoapp.view.ViewContract;
import io.github.nfdz.findoordemoapp.view.model.ViewInteractor;

public class ViewPresenter implements ViewContract.Presenter {

    private ViewContract.View view;
    private ViewContract.Interactor interactor;

    public ViewPresenter(ViewContract.View view, Context context) {
        this.view = view;
        this.interactor = new ViewInteractor(context);
    }

    @Override
    public void onCreate() {
        interactor.initialize();
        view.setLocation(interactor.getLocation());
    }

    @Override
    public void onDestroy() {
        if (interactor != null) {
            interactor.destroy();
            interactor = null;
        }
        view = null;
    }

    @Override
    public void onLoadLocationClick(@Nullable Integer location) {
        if (location != null) {
            view.lockLocation();
            interactor.loadLocation(location, new ViewContract.Interactor.LoadLocationCallback() {
                @Override
                public void onSuccess(int samplesTotal, long firstRecordTimestamp, long lastRecordTimestamp) {
                    if (view != null) {
                        view.setRecordsInfo(samplesTotal, firstRecordTimestamp, lastRecordTimestamp);
                    }
                }
            });
        } else {
            view.showInvalidLocationErrorMsg();
        }
    }

    @Override
    public void onCloseLocationClick() {
        onCloseIntervalRecordsClick();
        view.clearRecordsInfo();
        view.unlockLocation();
    }

    @Override
    public void onSetViewRecordsIntervalFrom(long fromTimestamp) {
        interactor.setIntervalViewFrom(fromTimestamp);
        view.setIntervalViewFrom(interactor.getIntervalViewFrom());
    }

    @Override
    public void onSetViewRecordsIntervalTo(long toTimestamp) {
        interactor.setIntervalViewTo(toTimestamp);
        view.setIntervalViewTo(interactor.getIntervalViewTo());
    }

    @Override
    public void onLoadIntervalRecordsClick() {
        view.lockViewRecordsInterval();
        interactor.loadRecords(new ViewContract.Interactor.LoadRecordsCallback() {
            @Override
            public void onSuccess(List<RecordsStatistics> records) {
                if (view != null) {
                    view.showRecords(records);
                }
            }
            @Override
            public void onIntervalError() {
                if (view != null) {
                    view.showInvalidIntervalErrorMsg();
                }
            }
            @Override
            public void onProcessingError() {
                if (view != null) {
                    view.showProcessingError();
                }
            }
        });
    }

    @Override
    public void onCloseIntervalRecordsClick() {
        view.showRecords(null);
        view.unlockViewRecordsInterval();
    }

}
