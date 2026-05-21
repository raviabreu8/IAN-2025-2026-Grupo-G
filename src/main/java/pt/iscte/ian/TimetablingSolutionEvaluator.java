package pt.iscte.ian;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TimetablingSolutionEvaluator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RoomFeatureMatcher featureMatcher = new RoomFeatureMatcher();

    public TimetablingSolutionEvaluation evaluate(TimetablingDataset dataset) {
        Set<String> knownRoomNames = dataset.getRooms()
                .stream()
                .map(Room::getName)
                .collect(Collectors.toSet());

        Map<String, Room> roomsByName = dataset.getRooms()
                .stream()
                .collect(Collectors.toMap(
                        Room::getName,
                        room -> room,
                        (first, second) -> first
                ));

        int totalEntries = dataset.getScheduleEntries().size();
        int invalidRoomAssignments = 0;
        int capacityViolations = 0;
        int totalCapacityShortage = 0;
        int missingRoomAssignments = 0;
        int unknownRoomAssignments = 0;
        int featureMismatches = 0;
        int totalUnusedCapacity = 0;

        Map<String, List<ScheduleInterval>> intervalsByRoomAndDate = new HashMap<>();
        Map<String, List<ScheduleInterval>> intervalsByClassGroupAndDate = new HashMap<>();

        for (ScheduleEntry entry : dataset.getScheduleEntries()) {
            String roomName = entry.getRoomName();
            boolean requiresRoom = !featureMatcher.isNoRoomNeeded(entry.getRequestedRoomFeature());
            boolean hasAssignedRoom = roomName != null && !roomName.isBlank();

            if (requiresRoom) {
                if (!hasAssignedRoom) {
                    missingRoomAssignments++;
                } else if (!knownRoomNames.contains(roomName)) {
                    unknownRoomAssignments++;
                }
            }

            if (requiresRoom && hasAssignedRoom && entry.getRoomCapacity() > 0) {
                int capacityDifference = entry.getRoomCapacity() - entry.getEnrolledStudents();

                if (capacityDifference < 0) {
                    capacityViolations++;
                    totalCapacityShortage += Math.abs(capacityDifference);
                } else {
                    totalUnusedCapacity += capacityDifference;
                }
            }

            if (requiresRoom && hasAssignedRoom) {
                Room assignedRoom = roomsByName.get(roomName);

                if (assignedRoom != null) {
                    String requestedFeature = entry.getRequestedRoomFeature();

                    if (requestedFeature != null
                            && !requestedFeature.isBlank()
                            && !featureMatcher.matches(requestedFeature, assignedRoom)) {
                        featureMismatches++;
                    }
                }
            }

            ScheduleInterval interval = toInterval(entry);

            if (interval == null) {
                continue;
            }

            if (hasAssignedRoom) {
                String roomDateKey = roomName + "|" + interval.date();

                intervalsByRoomAndDate
                        .computeIfAbsent(roomDateKey, key -> new ArrayList<>())
                        .add(interval);
            }

            String classGroup = entry.getClassGroup();

            if (classGroup != null && !classGroup.isBlank()) {
                String classGroupDateKey = classGroup + "|" + interval.date();

                intervalsByClassGroupAndDate
                        .computeIfAbsent(classGroupDateKey, key -> new ArrayList<>())
                        .add(interval);
            }
        }

        int roomTimeConflicts = countOverlappingIntervals(intervalsByRoomAndDate);
        int classGroupTimeConflicts = countOverlappingIntervals(intervalsByClassGroupAndDate);

        int totalPenalty =
                invalidRoomAssignments * TimetablingPenaltyWeights.INVALID_ROOM_ASSIGNMENT +
                missingRoomAssignments * TimetablingPenaltyWeights.MISSING_ROOM_ASSIGNMENT +
                unknownRoomAssignments * TimetablingPenaltyWeights.UNKNOWN_ROOM_ASSIGNMENT +
                capacityViolations * TimetablingPenaltyWeights.CAPACITY_VIOLATION +
                totalCapacityShortage * TimetablingPenaltyWeights.CAPACITY_SHORTAGE_PER_SEAT +
                roomTimeConflicts * TimetablingPenaltyWeights.ROOM_TIME_CONFLICT +
                featureMismatches * TimetablingPenaltyWeights.FEATURE_MISMATCH +
                totalUnusedCapacity * TimetablingPenaltyWeights.UNUSED_CAPACITY_PER_SEAT;

        return new TimetablingSolutionEvaluation(
                totalEntries,
                invalidRoomAssignments,
                capacityViolations,
                totalCapacityShortage,
                missingRoomAssignments,
                unknownRoomAssignments,
                roomTimeConflicts,
                classGroupTimeConflicts,
                featureMismatches,
                totalUnusedCapacity,
                totalPenalty
        );
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

    private int countOverlappingIntervals(Map<String, List<ScheduleInterval>> groupedIntervals) {
        int conflicts = 0;

        for (List<ScheduleInterval> intervals : groupedIntervals.values()) {
            intervals.sort(Comparator.comparing(ScheduleInterval::startTime));

            for (int i = 0; i < intervals.size(); i++) {
                ScheduleInterval current = intervals.get(i);

                for (int j = i + 1; j < intervals.size(); j++) {
                    ScheduleInterval next = intervals.get(j);

                    if (!next.startTime().isBefore(current.endTime())) {
                        break;
                    }

                    if (overlaps(current, next)) {
                        conflicts++;
                    }
                }
            }
        }

        return conflicts;
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
