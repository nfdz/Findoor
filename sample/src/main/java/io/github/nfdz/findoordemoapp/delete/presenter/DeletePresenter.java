package io.github.nfdz.findoordemoapp.delete.presenter;

import android.content.Context;
import android.support.annotation.Nullable;

import io.github.nfdz.findoordemoapp.delete.DeleteContract;
import io.github.nfdz.findoordemoapp.delete.model.DeleteInteractor;

public class DeletePresenter implements DeleteContract.Presenter, DeleteContract.Interactor.DeleteCallback {

    private DeleteContract.View view;
    private DeleteContract.Interactor interactor;

    public DeletePresenter(DeleteContract.View view, Context context) {
        this.view = view;
        interactor = new DeleteInteractor(context);
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
    public void onDeleteLocation(@Nullable final Integer location) {
        if (location != null) {
            view.askConfirmation(new DeleteContract.View.ConfirmationCallback() {
                @Override
                public void onConfirm() {
                    if (interactor != null) {
                        interactor.deleteLocation(location, DeletePresenter.this);
                    }
                }
            });
        } else {
            view.showInvalidLocationErrorMsg();
        }
    }

    @Override
    public void onDeleteIntervalClick(@Nullable final Integer location, @Nullable final Long fromTimestamp, @Nullable final Long toTimestamp) {
        if (location != null) {
            if (fromTimestamp == null || toTimestamp == null || fromTimestamp > toTimestamp) {
                view.showInvalidIntervalErrorMsg();
            } else {
                view.askConfirmation(new DeleteContract.View.ConfirmationCallback() {
                    @Override
                    public void onConfirm() {
                        if (interactor != null) {
                            interactor.deleteLocationInterval(location, fromTimestamp, toTimestamp, DeletePresenter.this);
                        }
                    }
                });
            }
        } else {
            view.showInvalidLocationErrorMsg();
        }
    }

    @Override
    public void onDeleteWithLessSamplesClick(@Nullable final Integer location, @Nullable final Integer samples) {
        if (location != null) {
            if (samples != null) {
                view.askConfirmation(new DeleteContract.View.ConfirmationCallback() {
                    @Override
                    public void onConfirm() {
                        if (interactor != null) {
                            interactor.deleteLocationWithLessSamples(location, samples, DeletePresenter.this);
                        }
                    }
                });
            } else {
                view.showInvalidSamplesErrorMsg();
            }
        } else {
            view.showInvalidLocationErrorMsg();
        }
    }

    @Override
    public void onDeleteAllClick() {
        view.askConfirmation(new DeleteContract.View.ConfirmationCallback() {
            @Override
            public void onConfirm() {
                if (interactor != null) {
                    interactor.deleteAll(DeletePresenter.this);
                }
            }
        });
    }

    @Override
    public void onSuccess() {
        if (view != null) {
            view.showDeleteSuccessMsg();
        }
    }

    @Override
    public void onError() {
        if (view != null) {
            view.showDeleteErrorMsg();
        }
    }

}
