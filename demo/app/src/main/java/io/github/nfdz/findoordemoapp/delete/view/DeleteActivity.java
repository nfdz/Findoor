package io.github.nfdz.findoordemoapp.delete.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.nfdz.findoordemoapp.R;
import io.github.nfdz.findoordemoapp.common.dialog.AskDateDialogFragment;
import io.github.nfdz.findoordemoapp.common.utils.PreferencesUtils;
import io.github.nfdz.findoordemoapp.delete.DeleteContract;
import io.github.nfdz.findoordemoapp.delete.presenter.DeletePresenter;

public class DeleteActivity extends AppCompatActivity implements DeleteContract.View {

    public static void start(Context context) {
        context.startActivity(new Intent(context, DeleteActivity.class));
    }

    @BindView(R.id.tv_location_label) TextView tv_location_label;
    @BindView(R.id.et_location) EditText et_location;
    @BindView(R.id.tv_interval_from_value) TextView tv_interval_from_value;
    @BindView(R.id.tv_interval_to_value) TextView tv_interval_to_value;
    @BindView(R.id.et_less_samples_value) EditText et_less_samples_value;

    private SimpleDateFormat timeFormat;
    private DeleteContract.Presenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);
        timeFormat = new SimpleDateFormat(getString(R.string.time_format_pattern), Locale.getDefault());
        ButterKnife.bind(this);
        setupToolbar();
        presenter = new DeletePresenter(this, this);
        presenter.onCreate();
    }

    private void setupToolbar() {
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(R.string.remove_location);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void showDeleteSuccessMsg() {
        Toast.makeText(this, R.string.delete_success_msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showDeleteErrorMsg() {
        Toast.makeText(this, R.string.delete_error_msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showInvalidSamplesErrorMsg() {
        Toast.makeText(this, R.string.samples_error_msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showInvalidLocationErrorMsg() {
        Toast.makeText(this, R.string.location_error_msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showInvalidIntervalErrorMsg() {
        Toast.makeText(this, R.string.interval_error_msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void askConfirmation(final ConfirmationCallback callback) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirmation)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onConfirm();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @OnClick(R.id.tv_interval_from_value)
    public void onIntevalFromClick() {
        AskDateDialogFragment dialog = AskDateDialogFragment.newInstance(tv_interval_from_value.getText().toString());
        dialog.setListener(new AskDateDialogFragment.DateListener() {
            @Override
            public void onSelectedDate(Date date) {
                try {
                    tv_interval_from_value.setText(timeFormat.format(date));
                } catch (Exception e) {
                    // swallow
                }
            }
        });
        dialog.show(getSupportFragmentManager(), "AskDateDialogFragmentFrom");
    }

    @OnClick(R.id.tv_interval_to_value)
    public void onIntevalToClick() {
        AskDateDialogFragment dialog = AskDateDialogFragment.newInstance(tv_interval_to_value.getText().toString());
        dialog.setListener(new AskDateDialogFragment.DateListener() {
            @Override
            public void onSelectedDate(Date date) {
                try {
                    tv_interval_to_value.setText(timeFormat.format(date));
                } catch (Exception e) {
                    // swallow
                }
            }
        });
        dialog.show(getSupportFragmentManager(), "AskDateDialogFragmentFrom");
    }

    @OnClick(R.id.btn_delete_location)
    public void onDeleteLocationClick() {
        presenter.onDeleteLocation(getLocation());
    }

    @OnClick(R.id.btn_delete_interval)
    public void onDeleteIntervalClick() {
        Long fromTimestamp = null;
        Long toTimestamp = null;
        try {
            fromTimestamp = timeFormat.parse(tv_interval_from_value.getText().toString()).getTime();
            toTimestamp = timeFormat.parse(tv_interval_to_value.getText().toString()).getTime();
        } catch (Exception e) {
            // swallow
        }
        presenter.onDeleteIntervalClick(getLocation(), fromTimestamp, toTimestamp);
    }

    @OnClick(R.id.btn_delete_with_less_samples)
    public void onDeleteWithLessSamplesClick() {
        Integer samples = null;
        try {
            samples = Integer.parseInt(et_less_samples_value.getText().toString());
        } catch (Exception e) {
            // swallow
        }
        presenter.onDeleteWithLessSamplesClick(getLocation(), samples);
    }

    @OnClick(R.id.btn_delete_all)
    public void onDeleteAllClick() {
        presenter.onDeleteAllClick();
    }

    private Integer getLocation() {
        Integer location = null;
        try {
            location = Integer.parseInt(et_location.getText().toString());
            showAliasIfCan(location);
        } catch (Exception e) {
            // swallow
        }
        return location;
    }

    private void showAliasIfCan(@Nullable Integer location) {
        if (location != null) {
            String alias = PreferencesUtils.getLocationAlias(this, location);
            if (!TextUtils.isEmpty(alias)) Toast.makeText(this, alias, Toast.LENGTH_LONG).show();
        }
    }

}
