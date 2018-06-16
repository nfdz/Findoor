package io.github.nfdz.findoor.model;

import java.util.List;

public class LocationComparison {

    public final int locationToTestId;

    public final int rawGapMean;
    public final int netGapMean;

    public final int ignoringRawGapMean;
    public final int ignoringNetGapMean;

    public final int similarityPercentage;

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
