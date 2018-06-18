package io.github.nfdz.findoordemoapp.view.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.nfdz.findoor.model.RecordsStatistics;
import io.github.nfdz.findoordemoapp.R;
import io.github.nfdz.findoordemoapp.common.dialog.AskDateDialogFragment;
import io.github.nfdz.findoordemoapp.common.utils.PreferencesUtils;
import io.github.nfdz.findoordemoapp.view.ViewContract;
import io.github.nfdz.findoordemoapp.view.presenter.ViewPresenter;

public class ViewActivity extends AppCompatActivity implements ViewContract.View {

    public static void start(Context context) {
        context.startActivity(new Intent(context, ViewActivity.class));
    }

    @BindView(R.id.et_location) EditText et_location;
    @BindView(R.id.btn_start_load) Button btn_start_load;
    @BindView(R.id.btn_stop_close) Button btn_stop_close;
    @BindView(R.id.tv_samples_value) TextView tv_samples_value;
    @BindView(R.id.tv_first_timestamp_value) TextView tv_first_timestamp_value;
    @BindView(R.id.tv_last_timestamp_value) TextView tv_last_timestamp_value;
    @BindView(R.id.tv_interval_from_value) TextView tv_interval_from_value;
    @BindView(R.id.tv_interval_to_value) TextView tv_interval_to_value;
    @BindView(R.id.btn_interval_view) Button btn_interval_view;
    @BindView(R.id.btn_interval_close) Button btn_interval_close;
    @BindView(R.id.recycler_view_records) RecyclerView recycler_view_records;

    private SimpleDateFormat timeFormat;
    private RecordsAdapter adapter;
    private ViewContract.Presenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        timeFormat = new SimpleDateFormat(getString(R.string.time_format_pattern), Locale.getDefault());
        ButterKnife.bind(this);
        setupView();
        presenter = new ViewPresenter(this, this);
        presenter.onCreate();
    }

    private void setupView() {
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        adapter = new RecordsAdapter();
        recycler_view_records.setLayoutManager(new LinearLayoutManager(this));
        recycler_view_records.setHasFixedSize(true);
        recycler_view_records.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        recycler_view_records.addItemDecoration(dividerItemDecoration);
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void setLocation(int location) {
        et_location.setText(String.valueOf(location));
    }

    @Override
    public void lockLocation() {
        et_location.setInputType(InputType.TYPE_NULL);
        et_location.setEnabled(false);
        btn_start_load.setVisibility(View.INVISIBLE);
        btn_stop_close.setVisibility(View.VISIBLE);
    }

    @Override
    public void unlockLocation() {
        et_location.setEnabled(true);
        et_location.setInputType(InputType.TYPE_CLASS_NUMBER);
        btn_start_load.setVisibility(View.VISIBLE);
        btn_stop_close.setVisibility(View.INVISIBLE);
    }

    @Override
    public void setRecordsInfo(int samplesTotal, long firstRecordTimestamp, long lastRecordTimestamp) {
        tv_samples_value.setText(getString(R.string.samples_value, samplesTotal));
        String firstTime = timeFormat.format(new Date(firstRecordTimestamp));
        tv_first_timestamp_value.setText(firstTime);
        String lastTime = timeFormat.format(new Date(lastRecordTimestamp));
        tv_last_timestamp_value.setText(lastTime);
        tv_interval_from_value.setText(firstTime);
        tv_interval_to_value.setText(lastTime);
        btn_interval_view.setEnabled(true);
        tv_interval_from_value.setOnClickListener(new IntervalFromTextClickListener());
        tv_interval_to_value.setOnClickListener(new IntervalToTextClickListener());
    }

    @Override
    public void clearRecordsInfo() {
        tv_samples_value.setText("");
        tv_first_timestamp_value.setText("");
        tv_last_timestamp_value.setText("");
        tv_interval_from_value.setText("");
        tv_interval_to_value.setText("");
        btn_interval_view.setEnabled(false);
        tv_interval_from_value.setOnClickListener(null);
        tv_interval_to_value.setOnClickListener(null);
    }

    @Override
    public void lockViewRecordsInterval() {
        tv_interval_from_value.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        tv_interval_to_value.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        tv_interval_from_value.setOnClickListener(null);
        tv_interval_to_value.setOnClickListener(null);
        btn_interval_view.setVisibility(View.INVISIBLE);
        btn_interval_close.setVisibility(View.VISIBLE);
    }

    @Override
    public void unlockViewRecordsInterval() {
        tv_interval_from_value.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        tv_interval_to_value.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        tv_interval_from_value.setOnClickListener(new IntervalFromTextClickListener());
        tv_interval_to_value.setOnClickListener(new IntervalToTextClickListener());
        btn_interval_view.setVisibility(View.VISIBLE);
        btn_interval_close.setVisibility(View.INVISIBLE);
    }

    @Override
    public void setIntervalViewFrom(long intervalFrom) {
        tv_interval_from_value.setText(timeFormat.format(new Date(intervalFrom)));
    }

    @Override
    public void setIntervalViewTo(long intervalTo) {
        tv_interval_to_value.setText(timeFormat.format(new Date(intervalTo)));
    }

    @Override
    public void showRecords(@Nullable List<RecordsStatistics> records) {
        adapter.setData(records);
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
    public void showProcessingError() {
        Toast.makeText(this, R.string.processing_error_msg, Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.btn_start_load)
    public void onLocationLoadClick() {
        Integer location = null;
        try {
            location = Integer.parseInt(et_location.getText().toString());
            showAliasIfCan(location);
        } catch (Exception e) {
            // swallow
        }
        presenter.onLoadLocationClick(location);
    }

    private void showAliasIfCan(@Nullable Integer location) {
        if (location != null) {
            String alias = PreferencesUtils.getLocationAlias(this, location);
            if (!TextUtils.isEmpty(alias)) Toast.makeText(this, alias, Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.btn_stop_close)
    public void onLocationCloseClick() {
        presenter.onCloseLocationClick();
    }

    private class IntervalFromTextClickListener implements View.OnClickListener, AskDateDialogFragment.DateListener {
        @Override
        public void onClick(View v) {
            AskDateDialogFragment dialog = AskDateDialogFragment.newInstance(tv_interval_from_value.getText().toString());
            dialog.setListener(this);
            dialog.show(getSupportFragmentManager(), "AskDateDialogFragmentFrom");
        }
        @Override
        public void onSelectedDate(Date date) {
            presenter.onSetViewRecordsIntervalFrom(date.getTime());
        }
    }

    private class IntervalToTextClickListener implements View.OnClickListener, AskDateDialogFragment.DateListener {
        @Override
        public void onClick(View v) {
            AskDateDialogFragment dialog = AskDateDialogFragment.newInstance(tv_interval_to_value.getText().toString());
            dialog.setListener(this);
            dialog.show(getSupportFragmentManager(), "AskDateDialogFragmentTo");
        }
        @Override
        public void onSelectedDate(Date date) {
            presenter.onSetViewRecordsIntervalTo(date.getTime());
        }
    }

    @OnClick(R.id.btn_interval_view)
    public void onViewRecordsClick() {
        presenter.onLoadIntervalRecordsClick();
    }

    @OnClick(R.id.btn_interval_close)
    public void onCloseRecordsClick() {
        presenter.onCloseIntervalRecordsClick();
    }

    public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.RecordViewHolder> {

        private List<RecordsStatistics> records;

        public void setData(@Nullable List<RecordsStatistics> records) {
            this.records = records;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(ViewActivity.this).inflate(R.layout.item_record_statistics_entry, parent, false);
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
            @BindView(R.id.tv_record_samples) TextView tv_record_samples;

            public RecordViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            public void bindRecord(RecordsStatistics recordStatistics) {
                tv_record_ssid.setText(recordStatistics.ssid);
                tv_record_bssid.setText(recordStatistics.bssid);
                tv_record_frequency.setText(String.valueOf(recordStatistics.frequency) + "MHz");
                tv_record_samples.setText(getString(R.string.samples_value, recordStatistics.samples));
                tv_rssi.setText(String.valueOf(recordStatistics.rssiMean) + "dBm ±" +
                        String.valueOf(recordStatistics.rssiSd) + "dBm");
                tv_record_level.setText(String.valueOf(recordStatistics.levelMean) + "% ±"+
                        String.valueOf(recordStatistics.levelSd) + "%");
            }

        }
    }


}
