package pt.iscte.ian;

public class DatasetQualityReport {

    private final int numberOfRooms;
    private final int numberOfScheduleEntries;
    private final int numberOfDistinctRoomsUsed;
    private final int entriesWithCapacityProblem;
    private final int entriesWithoutRoom;
    private final int entriesWithUnknownRoom;
    private final int entriesNotRequiringRoom;

    public DatasetQualityReport(
            int numberOfRooms,
            int numberOfScheduleEntries,
            int numberOfDistinctRoomsUsed,
            int entriesWithCapacityProblem,
            int entriesWithoutRoom,
            int entriesWithUnknownRoom,
            int entriesNotRequiringRoom
    ) {
        this.numberOfRooms = numberOfRooms;
        this.numberOfScheduleEntries = numberOfScheduleEntries;
        this.numberOfDistinctRoomsUsed = numberOfDistinctRoomsUsed;
        this.entriesWithCapacityProblem = entriesWithCapacityProblem;
        this.entriesWithoutRoom = entriesWithoutRoom;
        this.entriesWithUnknownRoom = entriesWithUnknownRoom;
        this.entriesNotRequiringRoom = entriesNotRequiringRoom;
    }

    public int getNumberOfRooms() {
        return numberOfRooms;
    }

    public int getNumberOfScheduleEntries() {
        return numberOfScheduleEntries;
    }

    public int getNumberOfDistinctRoomsUsed() {
        return numberOfDistinctRoomsUsed;
    }

    public int getEntriesWithCapacityProblem() {
        return entriesWithCapacityProblem;
    }

    public int getEntriesWithoutRoom() {
        return entriesWithoutRoom;
    }

    public int getEntriesWithUnknownRoom() {
        return entriesWithUnknownRoom;
    }

    public int getEntriesNotRequiringRoom() {
        return entriesNotRequiringRoom;
    }

    @Override
    public String toString() {
        return "DatasetQualityReport{" +
                "numberOfRooms=" + numberOfRooms +
                ", numberOfScheduleEntries=" + numberOfScheduleEntries +
                ", numberOfDistinctRoomsUsed=" + numberOfDistinctRoomsUsed +
                ", entriesWithCapacityProblem=" + entriesWithCapacityProblem +
                ", entriesWithoutRoom=" + entriesWithoutRoom +
                ", entriesWithUnknownRoom=" + entriesWithUnknownRoom +
                ", entriesNotRequiringRoom=" + entriesNotRequiringRoom +
                '}';
    }
}
