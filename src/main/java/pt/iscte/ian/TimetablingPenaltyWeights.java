package pt.iscte.ian;

public final class TimetablingPenaltyWeights {

    public static final int INVALID_ROOM_ASSIGNMENT = 10000;
    public static final int MISSING_ROOM_ASSIGNMENT = 10000;
    public static final int UNKNOWN_ROOM_ASSIGNMENT = 10000;

    public static final int CAPACITY_VIOLATION = 1000;
    public static final int CAPACITY_SHORTAGE_PER_SEAT = 100;

    public static final int ROOM_TIME_CONFLICT = 500;
    public static final int CLASS_GROUP_TIME_CONFLICT = 500;

    public static final int FEATURE_MISMATCH = 500;

    public static final int UNUSED_CAPACITY_PER_SEAT = 1;

    private TimetablingPenaltyWeights() {
    }
}
