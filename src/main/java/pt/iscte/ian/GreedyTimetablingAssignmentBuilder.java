package pt.iscte.ian;

import java.util.Comparator;
import java.util.List;

public class GreedyTimetablingAssignmentBuilder {

    public TimetablingAssignment build(TimetablingOptimizationInstance instance) {
        List<ScheduleEntry> entries = instance.getEntriesToOptimize();
        List<Room> rooms = instance.getCandidateRooms();

        int[] roomIndexes = new int[entries.size()];

        for (int i = 0; i < entries.size(); i++) {
            ScheduleEntry entry = entries.get(i);

            int selectedRoomIndex = findBestRoomIndex(entry, rooms);
            roomIndexes[i] = selectedRoomIndex;
        }

        return new TimetablingAssignment(roomIndexes);
    }

    private int findBestRoomIndex(ScheduleEntry entry, List<Room> rooms) {
        int enrolledStudents = entry.getEnrolledStudents();

        return rooms.stream()
                .filter(room -> room.getNormalCapacity() >= enrolledStudents)
                .min(Comparator.comparingInt(Room::getNormalCapacity))
                .map(rooms::indexOf)
                .orElseGet(() -> findLargestRoomIndex(rooms));
    }

    private int findLargestRoomIndex(List<Room> rooms) {
        int largestRoomIndex = 0;
        int largestCapacity = -1;

        for (int i = 0; i < rooms.size(); i++) {
            int capacity = rooms.get(i).getNormalCapacity();

            if (capacity > largestCapacity) {
                largestCapacity = capacity;
                largestRoomIndex = i;
            }
        }

        return largestRoomIndex;
    }
}