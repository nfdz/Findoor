package io.github.nfdz.findoor.model;

/**
 * This class contains computed statistics data of a network samples set.
 */
public class RecordsStatistics {

    /** SSID [https://en.wikipedia.org/wiki/Service_set_(802.11_network)#Extended_service_sets_(ESSs)] */
    public final String ssid;

    /** BSSID [https://en.wikipedia.org/wiki/Service_set_(802.11_network)#Basic_service_set_identification_(BSSID)] */
    public final String bssid;

    /** Mean of signal level [0,100] */
    public final int levelMean;

    /** Standard deviation of signal level [0,100] */
    public final int levelSd;

    /** Mean of RSSI level in dBm */
    public final int rssiMean;

    /** Standard deviation of RSSI level in dBm */
    public final int rssiSd;

    /** Network working frequency in MHz */
    public final int frequency;

    /** Count of samples to compute this statistics */
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
