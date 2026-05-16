package pt.iscte.ian;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TimetablingOptimizationInstanceBuilder {

    public TimetablingOptimizationInstance build(
            TimetablingDataset dataset,
            int maxEntriesForOptimization
    ) {
        Set<String> knownRoomNames = dataset.getRooms()
                .stream()
                .map(Room::getName)
                .collect(Collectors.toSet());

        List<ScheduleEntry> problematicEntries = dataset.getScheduleEntries()
                .stream()
                .filter(entry -> isProblematic(entry, knownRoomNames))
                .sorted(Comparator.comparingInt(this::problemPriority).reversed())
                .limit(maxEntriesForOptimization)
                .toList();

        List<Room> candidateRooms = dataset.getRooms()
                .stream()
                .filter(room -> room.getNormalCapacity() > 0)
                .sorted(Comparator.comparingInt(Room::getNormalCapacity))
                .toList();

        return new TimetablingOptimizationInstance(
                problematicEntries,
                candidateRooms
        );
    }

    private boolean isProblematic(ScheduleEntry entry, Set<String> knownRoomNames) {
        String roomName = entry.getRoomName();

        boolean missingRoom = roomName == null || roomName.isBlank();
        boolean unknownRoom = !missingRoom && !knownRoomNames.contains(roomName);
        boolean capacityProblem =
                !missingRoom &&
                entry.getRoomCapacity() > 0 &&
                entry.getEnrolledStudents() > entry.getRoomCapacity();

        return missingRoom || unknownRoom || capacityProblem;
    }

    private int problemPriority(ScheduleEntry entry) {
        int priority = 0;

        if (entry.getRoomName() == null || entry.getRoomName().isBlank()) {
            priority += 100;
        }

        if (entry.getEnrolledStudents() > entry.getRoomCapacity()) {
            priority += 50;
        }

        priority += entry.getEnrolledStudents();

        return priority;
    }
}