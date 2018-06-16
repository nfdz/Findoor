package io.github.nfdz.findoor.model;

public class RecordsComparison {

    public final String ssid;
    public final String bssid;

    public final int rawGap;
    public final int netGap;

    public final RecordsStatistics statisticsTarget;
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
