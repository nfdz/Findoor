package io.github.nfdz.findoordemoapp.common.model;

import android.support.annotation.NonNull;

public class WifiRecordDto {

    private int location;
    private @NonNull String ssid;
    private @NonNull String bssid;
    private int level;
    private int rssi;
    private int frequency;
    private long timestamp;

    public WifiRecordDto(int location,
                         @NonNull String ssid,
                         @NonNull String bssid,
                         int level,
                         int rssi,
                         int frequency,
                         long timestamp) {
        this.location = location;
        this.ssid = ssid;
        this.bssid = bssid;
        this.level = level;
        this.rssi = rssi;
        this.frequency = frequency;
        this.timestamp = timestamp;
    }

    public WifiRecord toModel() {
        return new WifiRecord(location,
                ssid,
                bssid,
                level,
                rssi,
                frequency,
                timestamp);
    }

}
