package io.github.nfdz.findoordemoapp.trial.view;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.nfdz.findoor.model.LocationComparison;
import io.github.nfdz.findoordemoapp.R;
import io.github.nfdz.findoordemoapp.common.utils.PermissionsHelper;
import io.github.nfdz.findoordemoapp.common.utils.PreferencesUtils;
import io.github.nfdz.findoordemoapp.trial.TrialContract;
import io.github.nfdz.findoordemoapp.trial.presenter.TrialPresenter;

public class TrialActivity extends AppCompatActivity implements TrialContract.View, PermissionsHelper.Callback {

    public static final List<String> PERMISSIONS_TO_REQUEST = Arrays.asList(
            Manifest.permission.ACCESS_FINE_LOCATION
    );

    public static void start(Context context) {
        context.startActivity(new Intent(context, TrialActivity.class));
    }

    @BindView(R.id.et_locations) EditText et_locations;
    @BindView(R.id.btn_start_trial) Button btn_start_trial;
    @BindView(R.id.btn_stop_trial) Button btn_stop_trial;
    @BindView(R.id.recycler_view_result) RecyclerView recycler_view_result;
    @BindView(R.id.tv_samples_value) TextView tv_samples_value;

    private TrialContract.Presenter presenter;
    private PermissionsHelper permissionsHelper;
    private ResultAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trial);
        ButterKnife.bind(this);
        setupView();
        permissionsHelper = new PermissionsHelper(this, PERMISSIONS_TO_REQUEST, this);
        presenter = new TrialPresenter(this, this);
        presenter.onCreate();
    }

    private void setupView() {
        setupRecyclerView();
        setupToolbar();
    }

    private void setupToolbar() {
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(R.string.try_location);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    @OnClick(R.id.btn_start_trial)
    public void onStartClick() {
        presenter.onStartClick(et_locations.getText().toString());
    }

    @OnClick(R.id.btn_stop_trial)
    public void onStopClick() {
        presenter.onStopClick();
    }

    private void setupRecyclerView() {
        adapter = new ResultAdapter();
        recycler_view_result.setLayoutManager(new LinearLayoutManager(this));
        recycler_view_result.setHasFixedSize(true);
        recycler_view_result.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        recycler_view_result.addItemDecoration(dividerItemDecoration);
    }

    @Override
    public void setLocations(String locations) {
        et_locations.setText("");
        et_locations.append(locations);
    }

    @Override
    public void setResult(List<LocationComparison> comparisons, int samples) {
        tv_samples_value.setText(String.valueOf(samples));
        adapter.setData(comparisons);
    }

    @Override
    public void showStartButton(boolean state) {
        btn_start_trial.setVisibility(state ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void showStopButton(boolean state) {
        btn_stop_trial.setVisibility(state ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void lockLocations() {
        et_locations.setInputType(InputType.TYPE_NULL);
        et_locations.setEnabled(false);
    }

    @Override
    public void unlockLocations() {
        et_locations.setEnabled(true);
        et_locations.setInputType(InputType.TYPE_CLASS_TEXT);
    }

    @Override
    public void showListLocationsErrorMsg() {
        Toast.makeText(this, R.string.list_locations_msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showPermissionsErrorMsg() {
        Toast.makeText(this, R.string.permissions_error_msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showInvalidLocationsErrorMsg() {
        Toast.makeText(this, R.string.locations_error_msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showWifiDisabledErrorMsg() {
        Toast.makeText(this, R.string.wifi_disabled_error_msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void enableWifiService() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager != null && !wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    @Override
    public void askLocationService() {
        // FIXME show this alert always because in some device isLocationEnabled does not work properly
//        if (!isLocationEnabled()) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.gps_disabled_title)
                .setMessage(R.string.gps_disabled_message)
                .setCancelable(false)
                .setPositiveButton(R.string.go_location_setting, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.close_location_dialog, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
//        }
    }

    private boolean isLocationEnabled() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            String provider = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !provider.equals("");
        } else {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean gps = false;
            boolean network = false;
            try {
                gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (Exception e) {
                // swallow
            }
            return gps || network;
        }
    }

    @Override
    public void askPermissions() {
        permissionsHelper.askPermissions();
    }

    @Override
    public void navigateToBack() {
        onBackPressed();
    }

    @Override
    public void onPermissionsGranted() {
        presenter.onPermissionsGranted();
    }

    @Override
    public void onPermissionsDenied() {
        presenter.onPermissionsDenied();
    }

    public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ComparisonViewHolder> {

        private List<LocationComparison> comparisons;

        public void setData(@Nullable List<LocationComparison> comparisons) {
            final List<LocationComparison> oldList = this.comparisons == null ? Collections.<LocationComparison>emptyList() : this.comparisons;
            final List<LocationComparison> newList = comparisons == null ? Collections.<LocationComparison>emptyList() : comparisons;
            DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return oldList.size();
                }
                @Override
                public int getNewListSize() {
                    return newList.size();
                }
                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    LocationComparison oldComparison = oldList.get(oldItemPosition);
                    LocationComparison newComparison = newList.get(newItemPosition);
                    return oldComparison.locationToTestId == newComparison.locationToTestId;
                }
                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return false;
                }
            });
            this.comparisons = comparisons;
            diff.dispatchUpdatesTo(this);
        }

        @NonNull
        @Override
        public ComparisonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(TrialActivity.this).inflate(R.layout.item_location_comparison, parent, false);
            return new ComparisonViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ComparisonViewHolder holder, int position) {
            holder.bindComparison(comparisons.get(position), position);
        }

        @Override
        public int getItemCount() {
            return comparisons != null ? comparisons.size() : 0;
        }

        public class ComparisonViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.tv_location_text) TextView tv_location_text;
            @BindView(R.id.tv_raw_gap_value) TextView tv_raw_gap_value;
            @BindView(R.id.tv_net_gap_value) TextView tv_net_gap_value;
            @BindView(R.id.tv_similarity_value) TextView tv_similarity_value;

            public ComparisonViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            public void bindComparison(LocationComparison comparison, int position) {
                String alias = PreferencesUtils.getLocationAlias(TrialActivity.this, comparison.locationToTestId);
                String locationText = (position + 1) + ") " + comparison.locationToTestId;
                if (!TextUtils.isEmpty(alias)) locationText += " - " + alias;
                tv_location_text.setText(locationText);
                String rawGapText = comparison.rawGapMean + "%";
                tv_raw_gap_value.setText(rawGapText);
                String netGapText = comparison.netGapMean + "%";
                tv_net_gap_value.setText(netGapText);
                String similarityText = comparison.similarityPercentage + "%";
                tv_similarity_value.setText(similarityText);
            }

        }
    }
}
