package io.github.nfdz.findoordemoapp.compare.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.nfdz.findoor.model.LocationComparison;
import io.github.nfdz.findoor.model.RecordsComparison;
import io.github.nfdz.findoordemoapp.R;
import io.github.nfdz.findoordemoapp.common.utils.PreferencesUtils;
import io.github.nfdz.findoordemoapp.compare.CompareContract;
import io.github.nfdz.findoordemoapp.compare.presenter.ComparePresenter;

public class CompareActivity extends AppCompatActivity implements CompareContract.View {

    public static void start(Context context) {
        context.startActivity(new Intent(context, CompareActivity.class));
    }

    @BindView(R.id.et_location_A) EditText et_location_A;
    @BindView(R.id.et_location_B) EditText et_location_B;
    @BindView(R.id.tv_total_raw_gap_value) TextView tv_total_raw_gap_value;
    @BindView(R.id.tv_total_net_gap_value) TextView tv_total_net_gap_value;
    @BindView(R.id.tv_total_raw_gap_ignoring_value) TextView tv_total_raw_gap_ignoring_value;
    @BindView(R.id.tv_total_net_gap_ignoring_value) TextView tv_total_net_gap_ignoring_value;
    @BindView(R.id.tv_similarity_value) TextView tv_similarity_value;
    @BindView(R.id.rv_comparisons) RecyclerView rv_comparisons;
    @BindView(R.id.container_loading) View container_loading;
    @BindView(R.id.btn_compare) Button btn_compare;

    private CompareContract.Presenter presenter;
    private ComparisonsAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);
        ButterKnife.bind(this);
        setupView();
        presenter = new ComparePresenter(this);
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
            ab.setTitle(R.string.compare_locations);
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

    private void setupRecyclerView() {
        adapter = new ComparisonsAdapter();
        rv_comparisons.setLayoutManager(new LinearLayoutManager(this));
        rv_comparisons.setHasFixedSize(true);
        rv_comparisons.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        rv_comparisons.addItemDecoration(dividerItemDecoration);
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    @OnClick(R.id.btn_compare)
    public void onCompareClick() {
        Integer locationA = null;
        Integer locationB = null;
        try {
            locationA = Integer.parseInt(et_location_A.getText().toString());
            locationB = Integer.parseInt(et_location_B.getText().toString());
            showAliasIfCan(locationA, locationB);
        } catch (Exception e) {
            // swallow
        }
        presenter.onCompareLocationClick(locationA, locationB);
    }

    private void showAliasIfCan(int locationA, int locationB) {
        String aliasA = PreferencesUtils.getLocationAlias(this, locationA);
        String aliasB = PreferencesUtils.getLocationAlias(this, locationB);
        String msg = (TextUtils.isEmpty(aliasA) ? String.valueOf(locationA) : aliasA) +
                "(target) vs " +
                (TextUtils.isEmpty(aliasB) ? String.valueOf(locationB) : aliasB) + "(test)";
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showLoading() {
        btn_compare.setEnabled(false);
        container_loading.setVisibility(View.VISIBLE);
        container_loading.bringToFront();
        container_loading.requestFocus();
    }

    @Override
    public void hideLoading() {
        container_loading.setVisibility(View.INVISIBLE);
        btn_compare.setEnabled(true);
    }

    @Override
    public void showInvalidLocationAErrorMsg() {
        String msg = getString(R.string.location_error_msg) + " (A) ";
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showInvalidLocationBErrorMsg() {
        String msg = getString(R.string.location_error_msg) + " (B) ";
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showProcessErrorMsg() {
        Toast.makeText(this, R.string.processing_error_msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void setComparisonData(LocationComparison comparison) {
        tv_total_raw_gap_value.setText(String.valueOf(comparison.rawGapMean) + "%");
        tv_total_net_gap_value.setText(String.valueOf(comparison.netGapMean) + "%");
        tv_total_raw_gap_ignoring_value.setText(String.valueOf(comparison.ignoringRawGapMean) + "%");
        tv_total_net_gap_ignoring_value.setText(String.valueOf(comparison.ignoringNetGapMean) + "%");
        tv_similarity_value.setText(String.valueOf(comparison.similarityPercentage) + "%");
        adapter.updateComparisons(comparison.comparisons);
    }

    public class ComparisonsAdapter extends RecyclerView.Adapter<ComparisonsAdapter.ComparisonViewHolder> {

        private List<RecordsComparison> comparisons;

        public void updateComparisons(@Nullable List<RecordsComparison> comparisons) {
            this.comparisons = comparisons;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ComparisonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(CompareActivity.this).inflate(R.layout.item_comparison, parent, false);
            return new ComparisonViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ComparisonViewHolder holder, int position) {
            holder.bindComparison(comparisons.get(position));
        }

        @Override
        public int getItemCount() {
            return comparisons != null ? comparisons.size() : 0;
        }

        public class ComparisonViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.tv_ssid) TextView tv_ssid;
            @BindView(R.id.tv_bssid) TextView tv_bssid;
            @BindView(R.id.tv_raw_gap_value) TextView tv_raw_gap_value;
            @BindView(R.id.tv_net_gap_value) TextView tv_net_gap_value;
            @BindView(R.id.tv_A_level) TextView tv_A_level;
            @BindView(R.id.tv_A_rssi) TextView tv_A_rssi;
            @BindView(R.id.tv_A_samples) TextView tv_A_samples;
            @BindView(R.id.tv_B_level) TextView tv_B_level;
            @BindView(R.id.tv_B_rssi) TextView tv_B_rssi;
            @BindView(R.id.tv_B_samples) TextView tv_B_samples;

            public ComparisonViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            public void bindComparison(RecordsComparison comparison) {
                tv_raw_gap_value.setText(String.valueOf(comparison.rawGap) + "%");
                tv_net_gap_value.setText(String.valueOf(comparison.netGap) + "%");
                tv_ssid.setText(comparison.ssid);
                tv_bssid.setText(comparison.bssid);
                // Target
                String levelTargetText = String.valueOf(comparison.statisticsTarget.levelMean) + "% ±"+ String.valueOf(comparison.statisticsTarget.levelSd) + "%";
                tv_A_level.setText(levelTargetText);
                String rssiTargetText = String.valueOf(comparison.statisticsTarget.rssiMean) + "dBm ±" + String.valueOf(comparison.statisticsTarget.rssiSd) + "dBm";
                tv_A_rssi.setText(rssiTargetText);
                tv_A_samples.setText(getString(R.string.samples_value, comparison.statisticsTarget.samples));
                // Test
                if (comparison.statisticsTest != null) {
                    String levelTestText = String.valueOf(comparison.statisticsTest.levelMean) + "% ±"+ String.valueOf(comparison.statisticsTest.levelSd) + "%";
                    tv_B_level.setText(levelTestText);
                    String rssiTestText = String.valueOf(comparison.statisticsTest.rssiMean) + "dBm ±" + String.valueOf(comparison.statisticsTest.rssiSd) + "dBm";
                    tv_B_rssi.setText(rssiTestText);
                    tv_B_samples.setText(getString(R.string.samples_value, comparison.statisticsTest.samples));
                } else {
                    tv_B_level.setText("0%");
                    tv_B_rssi.setText("0dBm");
                    tv_B_samples.setText(getString(R.string.samples_value, 0));
                }
            }
        }
    }

}
