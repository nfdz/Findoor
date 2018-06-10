package io.github.nfdz.findoor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.github.nfdz.findoor.model.DisabledWifiException;
import io.github.nfdz.findoor.model.Record;

public class FindoorRecorder {

    public interface Listener {
        void onNotifyRecords(List<Record> records, long recordTime);
    }

    private static final IntentFilter RECEIVER_FILTER = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    private static final long DEFAULT_SCAN_FREQUENCY_MILLIS = TimeUnit.SECONDS.toMillis(5);

    private final Context context;
    private final WifiManager wifiManager;
    private final WifiScanReceiver receiver;
    private final Handler scanHandler;
    private final StartScanTask scanTask;
    private final Map<Long,List<Record>> allRecords;

    private Listener listener;
    private long scanFrequencyMillis;

    public FindoorRecorder(Context context) {
        this.scanHandler = new Handler();
        this.context = context.getApplicationContext();
        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.receiver = new WifiScanReceiver();
        this.scanTask = new StartScanTask();
        this.scanFrequencyMillis = DEFAULT_SCAN_FREQUENCY_MILLIS;
        this.allRecords = new HashMap<>();
    }

    public void setScanFrecuency(long scanFrequencyMillis) {
        this.scanFrequencyMillis = scanFrequencyMillis;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void clear() {
        allRecords.clear();
    }

    public Map<Long,List<Record>> getAllRecords() {
        return Collections.unmodifiableMap(allRecords);
    }

    public void startRecord() throws DisabledWifiException {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            if (!wifiManager.isWifiEnabled()) {
                throw new DisabledWifiException();
            }
        }
        context.registerReceiver(receiver, RECEIVER_FILTER);
        wifiManager.startScan();
    }

    public void stopRecord() {
        try {
            context.unregisterReceiver(receiver);
        } catch (Exception e) {
            // swallow
        }
        if (scanTask != null) {
            scanHandler.removeCallbacks(scanTask);
        }
    }

    private void processScanResult(List<ScanResult> result) {
        List<Record> records = new ArrayList<>();
        long recordTime = System.currentTimeMillis();
        for (ScanResult scan : result) {
            int level = WifiManager.calculateSignalLevel(scan.level, 100);
            records.add(new Record(scan.SSID, scan.BSSID, level, scan.level, scan.frequency, recordTime));
        }
        records = Collections.unmodifiableList(records);
        allRecords.put(recordTime, records);
        if (listener != null) {
            listener.onNotifyRecords(records, recordTime);
        }
    }

    private class WifiScanReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                success = intent != null && intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED ,false);
            } else {
                success = true;
            }
            if (success && wifiManager != null) {
                processScanResult(wifiManager.getScanResults());
            }
            scanHandler.postDelayed(scanTask, scanFrequencyMillis);
        }
    }

    private class StartScanTask implements Runnable {
        @Override
        public void run() {
            if (wifiManager != null) {
                wifiManager.startScan();
            }
        }
    }

}
