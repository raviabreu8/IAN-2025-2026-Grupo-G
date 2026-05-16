package pt.iscte.ian;

import java.util.Arrays;

public class TimetablingAssignment {

    private final int[] roomIndexes;

    public TimetablingAssignment(int[] roomIndexes) {
        this.roomIndexes = Arrays.copyOf(roomIndexes, roomIndexes.length);
    }

    public int size() {
        return roomIndexes.length;
    }

    public int getRoomIndexForEntry(int entryIndex) {
        return roomIndexes[entryIndex];
    }

    public int[] getRoomIndexes() {
        return Arrays.copyOf(roomIndexes, roomIndexes.length);
    }
}