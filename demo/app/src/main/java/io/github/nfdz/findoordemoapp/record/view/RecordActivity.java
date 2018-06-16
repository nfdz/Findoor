package io.github.nfdz.findoordemoapp.record.view;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.nfdz.findoordemoapp.R;
import io.github.nfdz.findoordemoapp.common.model.WifiRecord;
import io.github.nfdz.findoordemoapp.common.utils.PermissionsHelper;
import io.github.nfdz.findoordemoapp.common.utils.PreferencesUtils;
import io.github.nfdz.findoordemoapp.record.RecordContract;
import io.github.nfdz.findoordemoapp.record.presenter.RecordPresenter;

public class RecordActivity extends AppCompatActivity implements RecordContract.View, PermissionsHelper.Callback {

    public static final List<String> PERMISSIONS_TO_REQUEST = Arrays.asList(
            Manifest.permission.ACCESS_FINE_LOCATION
    );

    public static void start(Context context) {
        context.startActivity(new Intent(context, RecordActivity.class));
    }

    @BindView(R.id.et_location) EditText et_location;
    @BindView(R.id.btn_start_record) Button btn_start_record;
    @BindView(R.id.btn_stop_record) Button btn_stop_record;
    @BindView(R.id.recycler_view_records) RecyclerView recycler_view_records;
    @BindView(R.id.record_time_label) TextView record_time_label;
    @BindView(R.id.record_time_value) TextView record_time_value;

    private PermissionsHelper permissionsHelper;
    private RecordContract.Presenter presenter;
    private RecordsAdapter recordsAdapter;
    private SimpleDateFormat timeFormat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        timeFormat = new SimpleDateFormat(getString(R.string.time_format_pattern), Locale.getDefault());
        ButterKnife.bind(this);
        setupView();
        permissionsHelper = new PermissionsHelper(this, PERMISSIONS_TO_REQUEST, this);
        presenter = new RecordPresenter(this, this);
        presenter.onCreate();
    }

    private void setupView() {
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        recordsAdapter = new RecordsAdapter();
        recycler_view_records.setLayoutManager(new LinearLayoutManager(this));
        recycler_view_records.setHasFixedSize(true);
        recycler_view_records.setAdapter(recordsAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        recycler_view_records.addItemDecoration(dividerItemDecoration);
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    private void showAliasIfCan(@Nullable Integer location) {
        if (location != null) {
            String alias = PreferencesUtils.getLocationAlias(this, location);
            if (!TextUtils.isEmpty(alias)) Toast.makeText(this, alias, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void setLocation(int location) {
        et_location.setText(String.valueOf(location));
    }

    @Override
    public void lockLocation() {
        et_location.setInputType(InputType.TYPE_NULL);
        et_location.setEnabled(false);
    }

    @Override
    public void unlockLocation() {
        et_location.setEnabled(true);
        et_location.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    @Override
    public void showStartButton(boolean state) {
        btn_start_record.setVisibility(state ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void showStopButton(boolean state) {
        btn_stop_record.setVisibility(state ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void showRecords(@Nullable List<WifiRecord> records, long recordTime) {
        boolean noRecords = records == null;
        if (noRecords) {
            record_time_label.setVisibility(View.INVISIBLE);
            record_time_value.setVisibility(View.INVISIBLE);
            recordsAdapter.setData(null);
        } else {
            record_time_label.setVisibility(View.VISIBLE);
            record_time_value.setVisibility(View.VISIBLE);
            record_time_value.setText(timeFormat.format(new Date(recordTime)));
            recordsAdapter.setData(records);
        }
    }

    @Override
    public void showPermissionsErrorMsg() {
        Toast.makeText(this, R.string.permissions_error_msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showInvalidLocationErrorMsg() {
        Toast.makeText(this, R.string.location_error_msg, Toast.LENGTH_LONG).show();
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
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.gps_disabled_title)
                    .setMessage(R.string.gps_disabled_message)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
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

    @OnClick(R.id.btn_start_record)
    void onStartRecordClick() {
        Integer location = null;
        try {
            location = Integer.parseInt(et_location.getText().toString());
            showAliasIfCan(location);
        } catch (Exception e) {
            // swallow
        }
        presenter.onStartRecordClick(location);
    }

    @OnClick(R.id.btn_stop_record)
    void onStopRecordClick() {
        presenter.onStopRecordClick();
    }

    @Override
    public void onPermissionsGranted() {
        presenter.onPermissionsGranted();
    }

    @Override
    public void onPermissionsDenied() {
        presenter.onPermissionsDenied();
    }

    public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.RecordViewHolder> {

        private List<WifiRecord> records;

        public void setData(@Nullable List<WifiRecord> records) {
            final List<WifiRecord> oldList = this.records == null ? Collections.<WifiRecord>emptyList() : this.records;
            final List<WifiRecord> newList = records == null ? Collections.<WifiRecord>emptyList() : records;
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
                    WifiRecord oldRecord = oldList.get(oldItemPosition);
                    WifiRecord newRecord = newList.get(newItemPosition);
                    return oldRecord.bssid.equals(newRecord.bssid);
                }
                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    WifiRecord oldRecord = oldList.get(oldItemPosition);
                    WifiRecord newRecord = newList.get(newItemPosition);
                    boolean sameSsid = oldRecord.ssid.equals(newRecord.ssid);
                    boolean sameBssid = oldRecord.bssid.equals(newRecord.bssid);
                    boolean sameFrequency = oldRecord.frequency == newRecord.frequency;
                    boolean sameLevel = oldRecord.level == newRecord.level;
                    boolean sameRssi = oldRecord.rssi == newRecord.rssi;
                    return sameSsid && sameBssid && sameFrequency && sameLevel && sameRssi;
                }
            });
            this.records = records;
            diff.dispatchUpdatesTo(this);
        }

        @NonNull
        @Override
        public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(RecordActivity.this).inflate(R.layout.item_record_entry, parent, false);
            return new RecordViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
            holder.bindRecord(records.get(position));
        }

        @Override
        public int getItemCount() {
            return records != null ? records.size() : 0;
        }

        public class RecordViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.tv_record_ssid) TextView tv_record_ssid;
            @BindView(R.id.tv_record_bssid) TextView tv_record_bssid;
            @BindView(R.id.tv_record_frequency) TextView tv_record_frequency;
            @BindView(R.id.tv_rssi) TextView tv_rssi;
            @BindView(R.id.tv_record_level) TextView tv_record_level;

            public RecordViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            public void bindRecord(WifiRecord record) {
                tv_record_ssid.setText(record.ssid);
                tv_record_bssid.setText(record.bssid);
                tv_record_frequency.setText(String.valueOf(record.frequency) + "MHz");
                tv_rssi.setText(String.valueOf(record.rssi) + "dBm");
                tv_record_level.setText(String.valueOf(record.level) + "%");
            }

        }
    }
}