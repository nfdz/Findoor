package io.github.nfdz.findoordemoapp.record.model;

import android.content.Context;
import android.support.annotation.Nullable;

import io.github.nfdz.findoor.FindoorRecorder;
import io.github.nfdz.findoordemoapp.record.RecordContract;

public class RecordInteractor implements RecordContract.Interactor {

    private FindoorRecorder recorder;

    public RecordInteractor(Context context) {
        recorder = new FindoorRecorder(context);
    }

    @Override
    public void initialize() {
//        recorder.startRecord();
    }

    @Override
    public void destroy() {

    }

    @Override
    public void setLocation(int location) {

    }

    @Override
    public int getLocation() {
        return 0;
    }

    @Override
    public void startRecord(@Nullable RecordProcessListener listener) {

    }

    @Override
    public void stopRecord() {

    }
}
