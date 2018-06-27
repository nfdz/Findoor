package io.github.nfdz.findoordemoapp.compare.presenter;

import android.support.annotation.Nullable;

import io.github.nfdz.findoor.model.LocationComparison;
import io.github.nfdz.findoordemoapp.compare.CompareContract;
import io.github.nfdz.findoordemoapp.compare.model.CompareInteractor;

public class ComparePresenter implements CompareContract.Presenter {

    private CompareContract.View view;
    private CompareContract.Interactor interactor;

    public ComparePresenter(CompareContract.View view) {
        this.view = view;
        this.interactor = new CompareInteractor();
    }

    @Override
    public void onCreate() {
        interactor.initialize();
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
    public void onCompareLocationClick(@Nullable Integer locationA, @Nullable Integer locationB) {
        if (locationA == null) {
            view.showInvalidLocationAErrorMsg();
        } else if (locationB == null) {
            view.showInvalidLocationBErrorMsg();
        } else {
            view.showLoading();
            interactor.compareLocation(locationA, locationB, new CompareContract.Interactor.CompareCallback() {
                @Override
                public void onSuccess(LocationComparison comparison) {
                    if (view != null) {
                        view.hideLoading();
                        view.setComparisonData(comparison);
                    }
                }
                @Override
                public void onError() {
                    if (view != null) {
                        view.hideLoading();
                        view.showProcessErrorMsg();
                    }
                }
            });
        }
    }

}
