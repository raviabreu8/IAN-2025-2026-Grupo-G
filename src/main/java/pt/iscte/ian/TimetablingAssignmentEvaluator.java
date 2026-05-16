package pt.iscte.ian;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TimetablingAssignmentEvaluator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final List<ScheduleEntry> entriesToOptimize;
    private final List<Room> candidateRooms;
    private final Map<String, List<ScheduleInterval>> fixedRoomOccupancy;

    public TimetablingAssignmentEvaluator(
            TimetablingDataset dataset,
            TimetablingOptimizationInstance instance
    ) {
        this.entriesToOptimize = instance.getEntriesToOptimize();
        this.candidateRooms = instance.getCandidateRooms();
        this.fixedRoomOccupancy = buildFixedRoomOccupancy(dataset, entriesToOptimize);
    }

    public TimetablingAssignmentEvaluation evaluate(TimetablingAssignment assignment) {
        Map<String, List<ScheduleInterval>> assignedRoomOccupancy = new HashMap<>();

        int assignedEntries = assignment.size();
        int invalidRoomAssignments = 0;
        int capacityViolations = 0;
        int totalCapacityShortage = 0;
        int roomTimeConflicts = 0;
        int totalUnusedCapacity = 0;

        for (int i = 0; i < assignment.size(); i++) {
            ScheduleEntry entry = entriesToOptimize.get(i);
            int roomIndex = assignment.getRoomIndexForEntry(i);

            if (roomIndex < 0 || roomIndex >= candidateRooms.size()) {
                invalidRoomAssignments++;
                continue;
            }

            Room selectedRoom = candidateRooms.get(roomIndex);

            int capacityDifference = selectedRoom.getNormalCapacity() - entry.getEnrolledStudents();

            if (capacityDifference < 0) {
                capacityViolations++;
                totalCapacityShortage += Math.abs(capacityDifference);
            } else {
                totalUnusedCapacity += capacityDifference;
            }

            ScheduleInterval interval = toInterval(entry);

            if (interval == null) {
                continue;
            }

            String roomDateKey = selectedRoom.getName() + "|" + interval.date();

            List<ScheduleInterval> fixedIntervals = fixedRoomOccupancy.getOrDefault(
                    roomDateKey,
                    List.of()
            );

            for (ScheduleInterval fixedInterval : fixedIntervals) {
                if (overlaps(fixedInterval, interval)) {
                    roomTimeConflicts++;
                }
            }

            List<ScheduleInterval> assignedIntervals = assignedRoomOccupancy.computeIfAbsent(
                    roomDateKey,
                    key -> new ArrayList<>()
            );

            for (ScheduleInterval assignedInterval : assignedIntervals) {
                if (overlaps(assignedInterval, interval)) {
                    roomTimeConflicts++;
                }
            }

            assignedIntervals.add(interval);
        }

        int totalPenalty =
                invalidRoomAssignments * 10000 +
                capacityViolations * 1000 +
                totalCapacityShortage * 100 +
                roomTimeConflicts * 50 +
                totalUnusedCapacity;

        return new TimetablingAssignmentEvaluation(
                assignedEntries,
                invalidRoomAssignments,
                capacityViolations,
                totalCapacityShortage,
                roomTimeConflicts,
                totalUnusedCapacity,
                totalPenalty
        );
    }

    private Map<String, List<ScheduleInterval>> buildFixedRoomOccupancy(
            TimetablingDataset dataset,
            List<ScheduleEntry> entriesToOptimize
    ) {
        Set<ScheduleEntry> optimizedEntries = Collections.newSetFromMap(new IdentityHashMap<>());
        optimizedEntries.addAll(entriesToOptimize);

        Map<String, List<ScheduleInterval>> roomOccupancy = new HashMap<>();

        for (ScheduleEntry entry : dataset.getScheduleEntries()) {
            if (optimizedEntries.contains(entry)) {
                continue;
            }

            String roomName = entry.getRoomName();

            if (roomName == null || roomName.isBlank()) {
                continue;
            }

            ScheduleInterval interval = toInterval(entry);

            if (interval == null) {
                continue;
            }

            String roomDateKey = roomName + "|" + interval.date();

            roomOccupancy.computeIfAbsent(roomDateKey, key -> new ArrayList<>())
                    .add(interval);
        }

        return roomOccupancy;
    }

    private ScheduleInterval toInterval(ScheduleEntry entry) {
        try {
            LocalDate date = LocalDate.parse(entry.getDate(), DATE_FORMATTER);
            LocalTime startTime = LocalTime.parse(entry.getStartTime());
            LocalTime endTime = LocalTime.parse(entry.getEndTime());

            if (!endTime.isAfter(startTime)) {
                return null;
            }

            return new ScheduleInterval(date, startTime, endTime);

        } catch (Exception e) {
            return null;
        }
    }

    private boolean overlaps(ScheduleInterval first, ScheduleInterval second) {
        return first.startTime().isBefore(second.endTime())
                && second.startTime().isBefore(first.endTime());
    }

    private record ScheduleInterval(
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime
    ) {
    }
}
