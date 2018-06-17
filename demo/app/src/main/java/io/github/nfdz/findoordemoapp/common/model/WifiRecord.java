package io.github.nfdz.findoordemoapp.common.model;

import android.support.annotation.NonNull;

import java.util.Date;

import io.github.nfdz.findoor.model.Record;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class WifiRecord extends RealmObject {

    public static WifiRecord buildFromRecord(int locationId, Record record) {
        return new WifiRecord(locationId,
                record.ssid,
                record.bssid,
                record.level,
                record.rssi,
                record.frequency,
                record.timestamp);
    }

    @PrimaryKey
    public String recordId;
    public final static String RECORD_ID_FIELD = "recordId";

    public int location;
    public final static String LOCATION_FIELD = "location";

    public String ssid;
    public final static String SSID_FIELD = "ssid";

    public String bssid;
    public final static String BSSID_FIELD = "bssid";

    // [0,100]
    public int level;
    public final static String LEVEL_FIELD = "level";

    // dBm
    public int rssi;
    public final static String RSSI_FIELD = "rssi";

    // MHz
    public int frequency;
    public final static String FREQUENCY_FIELD = "frequency";

    // Millis from 1970
    public long timestamp;
    public final static String TIMESTAMP_FIELD = "timestamp";

    public WifiRecord() {
        this.location = -1;
        this.recordId = "";
        this.ssid = "";
        this.bssid = "";
        this.level = 0;
        this.rssi = 0;
        this.frequency = 0;
        this.timestamp = 0;
    }

    public WifiRecord(int location,
                      @NonNull String ssid,
                      @NonNull String bssid,
                      int level,
                      int rssi,
                      int frequency,
                      long timestamp) {
        this.location = location;
        this.recordId = createRecordId(bssid, timestamp, location);
        this.ssid = ssid;
        this.bssid = bssid;
        this.level = level;
        this.rssi = rssi;
        this.frequency = frequency;
        this.timestamp = timestamp;
    }

    private String createRecordId(String bssid, long timestamp, int location) {
        return bssid + "-" + timestamp + "-" + location;
    }

    @Override
    public String toString() {
        return ssid + "(" + bssid + ", " + frequency + "MHz): " + level + "%" + ", " +
                rssi + "dBm, " + new Date(timestamp);
    }

}
