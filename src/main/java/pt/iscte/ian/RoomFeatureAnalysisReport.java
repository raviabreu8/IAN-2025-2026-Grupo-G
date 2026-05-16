package pt.iscte.ian;

public class RoomFeatureAnalysisReport {

    private final int entriesWithRequestedFeature;
    private final int noRoomNeededEntries;
    private final int entriesEvaluatedWithKnownRoom;
    private final int featureMatches;
    private final int featureMismatches;
    private final int entriesSkippedWithoutRoomOrUnknownRoom;

    public RoomFeatureAnalysisReport(
            int entriesWithRequestedFeature,
            int noRoomNeededEntries,
            int entriesEvaluatedWithKnownRoom,
            int featureMatches,
            int featureMismatches,
            int entriesSkippedWithoutRoomOrUnknownRoom
    ) {
        this.entriesWithRequestedFeature = entriesWithRequestedFeature;
        this.noRoomNeededEntries = noRoomNeededEntries;
        this.entriesEvaluatedWithKnownRoom = entriesEvaluatedWithKnownRoom;
        this.featureMatches = featureMatches;
        this.featureMismatches = featureMismatches;
        this.entriesSkippedWithoutRoomOrUnknownRoom = entriesSkippedWithoutRoomOrUnknownRoom;
    }

    public int getEntriesWithRequestedFeature() {
        return entriesWithRequestedFeature;
    }

    public int getNoRoomNeededEntries() {
        return noRoomNeededEntries;
    }

    public int getEntriesEvaluatedWithKnownRoom() {
        return entriesEvaluatedWithKnownRoom;
    }

    public int getFeatureMatches() {
        return featureMatches;
    }

    public int getFeatureMismatches() {
        return featureMismatches;
    }

    public int getEntriesSkippedWithoutRoomOrUnknownRoom() {
        return entriesSkippedWithoutRoomOrUnknownRoom;
    }

    public double getMismatchRate() {
        int evaluatedEntries = featureMatches + featureMismatches;

        if (evaluatedEntries == 0) {
            return 0;
        }

        return (double) featureMismatches / evaluatedEntries;
    }

    @Override
    public String toString() {
        return "RoomFeatureAnalysisReport{" +
                "entriesWithRequestedFeature=" + entriesWithRequestedFeature +
                ", noRoomNeededEntries=" + noRoomNeededEntries +
                ", entriesEvaluatedWithKnownRoom=" + entriesEvaluatedWithKnownRoom +
                ", featureMatches=" + featureMatches +
                ", featureMismatches=" + featureMismatches +
                ", entriesSkippedWithoutRoomOrUnknownRoom=" + entriesSkippedWithoutRoomOrUnknownRoom +
                ", mismatchRate=" + getMismatchRate() +
                '}';
    }
}
