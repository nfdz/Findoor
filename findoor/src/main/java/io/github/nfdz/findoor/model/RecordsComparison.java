package io.github.nfdz.findoor.model;

/**
 * This class contains all the information about the comparison of a specific network.
 */
public class RecordsComparison {

    /** SSID [https://en.wikipedia.org/wiki/Service_set_(802.11_network)#Extended_service_sets_(ESSs)] */
    public final String ssid;

    /** BSSID [https://en.wikipedia.org/wiki/Service_set_(802.11_network)#Basic_service_set_identification_(BSSID)] */
    public final String bssid;

    /** Raw signal gap (independent of others networks) */
    public final int rawGap;

    /** Net signal gap */
    public final int netGap;

    /** Records statistics of target location */
    public final RecordsStatistics statisticsTarget;

    /** Records statistics of test location */
    public final RecordsStatistics statisticsTest;

    public RecordsComparison(String ssid,
                             String bssid,
                             int rawGap,
                             int netGap,
                             RecordsStatistics statisticsTarget,
                             RecordsStatistics statisticsTest) {
        this.ssid = ssid;
        this.bssid = bssid;
        this.rawGap = rawGap;
        this.netGap = netGap;
        this.statisticsTarget = statisticsTarget;
        this.statisticsTest = statisticsTest;
    }

}
