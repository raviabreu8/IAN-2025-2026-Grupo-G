package pt.iscte.ian;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TimetablingEvaluator {

    public TimetablingEvaluation evaluate(TimetablingDataset dataset) {
        Set<String> knownRoomNames = dataset.getRooms()
                .stream()
                .map(Room::getName)
                .collect(Collectors.toSet());

        int totalEntries = dataset.getScheduleEntries().size();
        int capacityViolations = 0;
        int missingRoomAssignments = 0;
        int unknownRoomAssignments = 0;

        Map<String, Integer> roomTimeUsage = new HashMap<>();

        for (ScheduleEntry entry : dataset.getScheduleEntries()) {
            String roomName = entry.getRoomName();

            if (roomName == null || roomName.isBlank()) {
                missingRoomAssignments++;
                continue;
            }

            if (!knownRoomNames.contains(roomName)) {
                unknownRoomAssignments++;
            }

            if (entry.getEnrolledStudents() > entry.getRoomCapacity()) {
                capacityViolations++;
            }

            String roomTimeKey = roomName + "|" +
                    entry.getDate() + "|" +
                    entry.getStartTime() + "|" +
                    entry.getEndTime();

            roomTimeUsage.put(
                    roomTimeKey,
                    roomTimeUsage.getOrDefault(roomTimeKey, 0) + 1
            );
        }

        int roomTimeConflicts = 0;

        for (int usageCount : roomTimeUsage.values()) {
            if (usageCount > 1) {
                roomTimeConflicts += usageCount - 1;
            }
        }

        int totalPenalty =
                capacityViolations * 3 +
                missingRoomAssignments * 5 +
                unknownRoomAssignments * 4 +
                roomTimeConflicts * 5;

        return new TimetablingEvaluation(
                totalEntries,
                capacityViolations,
                missingRoomAssignments,
                unknownRoomAssignments,
                roomTimeConflicts,
                totalPenalty
        );
    }
}