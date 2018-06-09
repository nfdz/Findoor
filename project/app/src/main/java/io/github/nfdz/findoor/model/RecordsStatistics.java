package io.github.nfdz.findoor.model;

public class RecordsStatistics {

    public final String ssid;
    public final String bssid;
    public final int levelMean;
    public final int levelSd;
    public final int rssiMean;
    public final int rssiSd;
    public final int frequency;
    public final int samples;

    public RecordsStatistics(String ssid,
                             String bssid,
                             int levelMean,
                             int levelSd,
                             int rssiMean,
                             int rssiSd,
                             int frequency,
                             int samples) {
        this.ssid = ssid;
        this.bssid = bssid;
        this.levelMean = levelMean;
        this.levelSd = levelSd;
        this.rssiMean = rssiMean;
        this.rssiSd = rssiSd;
        this.frequency = frequency;
        this.samples = samples;
    }

}
