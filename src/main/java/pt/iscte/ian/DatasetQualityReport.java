package pt.iscte.ian;

public class DatasetQualityReport {

    private final int numberOfRooms;
    private final int numberOfScheduleEntries;
    private final int numberOfDistinctRoomsUsed;
    private final int entriesWithCapacityProblem;
    private final int entriesWithoutRoom;
    private final int entriesWithUnknownRoom;

    public DatasetQualityReport(
            int numberOfRooms,
            int numberOfScheduleEntries,
            int numberOfDistinctRoomsUsed,
            int entriesWithCapacityProblem,
            int entriesWithoutRoom,
            int entriesWithUnknownRoom
    ) {
        this.numberOfRooms = numberOfRooms;
        this.numberOfScheduleEntries = numberOfScheduleEntries;
        this.numberOfDistinctRoomsUsed = numberOfDistinctRoomsUsed;
        this.entriesWithCapacityProblem = entriesWithCapacityProblem;
        this.entriesWithoutRoom = entriesWithoutRoom;
        this.entriesWithUnknownRoom = entriesWithUnknownRoom;
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

    @Override
    public String toString() {
        return "DatasetQualityReport{" +
                "numberOfRooms=" + numberOfRooms +
                ", numberOfScheduleEntries=" + numberOfScheduleEntries +
                ", numberOfDistinctRoomsUsed=" + numberOfDistinctRoomsUsed +
                ", entriesWithCapacityProblem=" + entriesWithCapacityProblem +
                ", entriesWithoutRoom=" + entriesWithoutRoom +
                ", entriesWithUnknownRoom=" + entriesWithUnknownRoom +
                '}';
    }
}