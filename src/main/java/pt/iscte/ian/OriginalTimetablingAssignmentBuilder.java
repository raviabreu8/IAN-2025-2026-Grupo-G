package pt.iscte.ian;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OriginalTimetablingAssignmentBuilder {

    public TimetablingAssignment build(TimetablingOptimizationInstance instance) {
        List<ScheduleEntry> entries = instance.getEntriesToOptimize();
        List<Room> candidateRooms = instance.getCandidateRooms();

        Map<String, Integer> roomIndexByName = buildRoomIndexByName(candidateRooms);

        int[] roomIndexes = new int[entries.size()];

        for (int i = 0; i < entries.size(); i++) {
            ScheduleEntry entry = entries.get(i);
            String originalRoomName = entry.getRoomName();

            if (originalRoomName == null || originalRoomName.isBlank()) {
                roomIndexes[i] = -1;
                continue;
            }

            roomIndexes[i] = roomIndexByName.getOrDefault(originalRoomName, -1);
        }

        return new TimetablingAssignment(roomIndexes);
    }

    private Map<String, Integer> buildRoomIndexByName(List<Room> rooms) {
        Map<String, Integer> roomIndexByName = new HashMap<>();

        for (int i = 0; i < rooms.size(); i++) {
            roomIndexByName.put(rooms.get(i).getName(), i);
        }

        return roomIndexByName;
    }
}
