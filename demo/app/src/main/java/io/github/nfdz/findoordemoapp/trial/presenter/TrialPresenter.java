package io.github.nfdz.findoordemoapp.trial.presenter;

import android.content.Context;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.github.nfdz.findoor.model.LocationComparison;
import io.github.nfdz.findoordemoapp.trial.TrialContract;
import io.github.nfdz.findoordemoapp.trial.model.TrialInteractor;

public class TrialPresenter implements TrialContract.Presenter,
        TrialContract.Interactor.GetAllLocationsCallback,
        TrialContract.Interactor.TrialProcessListener {

    private TrialContract.View view;
    private TrialContract.Interactor interactor;

    public TrialPresenter(TrialContract.View view, Context context) {
        this.view = view;
        interactor = new TrialInteractor(context);
    }

    @Override
    public void onCreate() {
        interactor.initialize();
        view.showStartButton(true);
        view.showStopButton(false);
        interactor.getAllLocations(this);
        view.enableWifiService();
        view.askLocationService();
    }

    private String processLocations(List<Integer> locations) {
        Iterator<Integer> it = locations.iterator();
        StringBuilder bld = new StringBuilder();
        while (it.hasNext()) {
            bld.append(it.next());
            if (it.hasNext()) bld.append(", ");
        }
        return bld.toString();
    }

    @Override
    public void onDestroy() {
        if (interactor != null) {
            interactor.destroy();
            interactor= null;
        }
        view = null;
    }

    @Override
    public void onStartClick(@Nullable String locations) {
        List<Integer> processedLocations = processLocations(locations);
        if (processedLocations != null && processedLocations.size() > 1) {
            interactor.setLocations(processedLocations);
            view.askPermissions();
        } else {
            view.showInvalidLocationsErrorMsg();
        }
    }

    private void startTrial() {
        view.setResult(null, 0);
        view.lockLocations();
        view.showStartButton(false);
        view.showStopButton(true);
        interactor.startTrial(this);
    }

    private List<Integer> processLocations(String locations) {
        try {
            List<Integer> result = new ArrayList<>();
            String[] splitted = locations.split(",");
            for (String locationString : splitted) {
                result.add(Integer.parseInt(locationString.trim()));
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onStopClick() {
        interactor.stopTrial();
        view.unlockLocations();
        view.showStartButton(true);
        view.showStopButton(false);
    }

    @Override
    public void onPermissionsGranted() {
        startTrial();
    }

    @Override
    public void onPermissionsDenied() {
        view.showPermissionsErrorMsg();
        view.navigateToBack();
    }

    @Override
    public void onWifiDisabledError() {
        if (view != null) {
            view.showWifiDisabledErrorMsg();
            onStopClick();
        }
    }

    @Override
    public void onNotifyResult(List<LocationComparison> comparisons, int samples) {
        if (view != null) {
            view.setResult(comparisons, samples);
        }
    }

    @Override
    public void onFinish(List<Integer> locations) {
        if (view != null) {
            view.setLocations(processLocations(locations));
        }
    }

    @Override
    public void onError() {
        if (view != null) {
            view.showListLocationsErrorMsg();
        }
    }

}
