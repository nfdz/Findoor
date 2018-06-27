package io.github.nfdz.findoor.model;

import java.util.List;

/**
 * This class contains all information about the comparison of a location.
 */
public class LocationComparison {

    /** Test identifier of the location to test against target location */
    public final int locationToTestId;

    /** Mean of all raw signal gaps of each SSID */
    public final int rawGapMean;

    /** Mean of all net signal gaps of each SSID */
    public final int netGapMean;

    /** Mean of all raw signal gaps of each SSID ignoring turned off networks */
    public final int ignoringRawGapMean;

    /** Mean of all net signal gaps of each SSID ignoring turned off networks */
    public final int ignoringNetGapMean;

    /**
     * Similarity percentage of the set of detected networks,
     * weight according to their importance (level) in target
     */
    public final int similarityPercentage;

    /** List of each network comparison */
    public final List<RecordsComparison> comparisons;

    public LocationComparison(int locationToTestId,
                              int rawGapMean,
                              int netGapMean,
                              int ignoringRawGapMean,
                              int ignoringNetGapMean,
                              int similarityPercentage,
                              List<RecordsComparison> comparisons) {
        this.locationToTestId = locationToTestId;
        this.rawGapMean = rawGapMean;
        this.netGapMean = netGapMean;
        this.ignoringRawGapMean = ignoringRawGapMean;
        this.ignoringNetGapMean = ignoringNetGapMean;
        this.similarityPercentage = similarityPercentage;
        this.comparisons = comparisons;
    }

}
