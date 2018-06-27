package io.github.nfdz.findoordemoapp;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

import io.realm.Realm;
import timber.log.Timber;

public class DemoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        Realm.init(this);
        if (BuildConfig.DEBUG) {
            Timber.uprootAll();
            Timber.plant(new Timber.DebugTree());
        }
    }

}