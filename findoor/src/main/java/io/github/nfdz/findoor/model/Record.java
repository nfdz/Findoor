package io.github.nfdz.findoor.model;

import java.util.Date;

/**
 * This class contains all the data of an instant scan of a network.
 */
public class Record {

    /** SSID [https://en.wikipedia.org/wiki/Service_set_(802.11_network)#Extended_service_sets_(ESSs)] */
    public final String ssid;

    /** BSSID [https://en.wikipedia.org/wiki/Service_set_(802.11_network)#Basic_service_set_identification_(BSSID)] */
    public final String bssid;

    /** Signal level [0,100] (independent of the device) */
    public final int level;

    /** RSSI level in dBm [https://en.wikipedia.org/wiki/Received_signal_strength_indication] */
    public final int rssi;

    /** Network working frequency in MHz */
    public final int frequency;

    /** Timestamp of the record in millis from 1970 */
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
