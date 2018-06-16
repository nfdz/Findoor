package io.github.nfdz.findoor.model;

import java.util.Date;

public class Record {

    public final String ssid;

    public final String bssid;

    // [0,100]
    public final int level;

    // dBm
    public final int rssi;

    // MHz
    public final int frequency;

    // Millis from 1970
    public final long timestamp;

    public Record(String ssid,
                  String bssid,
                  int level,
                  int rssi,
                  int frequency,
                  long timestamp) {
        this.ssid = ssid;
        this.bssid = bssid;
        this.level = level;
        this.rssi = rssi;
        this.frequency = frequency;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return ssid + "(" + bssid + ", " + frequency + "MHz): " + level + "%" + ", " +
                rssi + "dBm, " + new Date(timestamp);
    }

}
