package pt.iscte.ian;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class OptimizedTimetablingDatasetBuilder {

    public TimetablingDataset build(
            TimetablingDataset originalDataset,
            TimetablingOptimizationInstance instance,
            OptimizationResult result
    ) {
        if (!result.hasBestPenaltyRoomIndexes()) {
            return originalDataset;
        }

        Map<ScheduleEntry, Room> optimizedRoomByEntry =
                buildOptimizedRoomMap(instance, result);

        List<ScheduleEntry> optimizedScheduleEntries = new ArrayList<>();

        for (ScheduleEntry entry : originalDataset.getScheduleEntries()) {
            Room optimizedRoom = optimizedRoomByEntry.get(entry);

            if (optimizedRoom == null) {
                optimizedScheduleEntries.add(entry);
            } else {
                optimizedScheduleEntries.add(createEntryWithOptimizedRoom(entry, optimizedRoom));
            }
        }

        return new TimetablingDataset(
                originalDataset.getRooms(),
                optimizedScheduleEntries
        );
    }

    private Map<ScheduleEntry, Room> buildOptimizedRoomMap(
            TimetablingOptimizationInstance instance,
            OptimizationResult result
    ) {
        Map<ScheduleEntry, Room> optimizedRoomByEntry = new IdentityHashMap<>();

        List<ScheduleEntry> entries = instance.getEntriesToOptimize();
        List<Room> rooms = instance.getCandidateRooms();
        int[] roomIndexes = result.getBestPenaltyRoomIndexes();

        int limit = Math.min(entries.size(), roomIndexes.length);

        for (int i = 0; i < limit; i++) {
            int roomIndex = roomIndexes[i];

            if (roomIndex < 0 || roomIndex >= rooms.size()) {
                continue;
            }

            optimizedRoomByEntry.put(entries.get(i), rooms.get(roomIndex));
        }

        return optimizedRoomByEntry;
    }

    private ScheduleEntry createEntryWithOptimizedRoom(
            ScheduleEntry originalEntry,
            Room optimizedRoom
    ) {
        String optimizedRoomFeaturesText = new TreeSet<>(optimizedRoom.getFeatures())
                .stream()
                .collect(Collectors.joining(", "));

        return new ScheduleEntry(
                originalEntry.getCourse(),
                originalEntry.getCurricularUnit(),
                originalEntry.getShift(),
                originalEntry.getClassGroup(),
                originalEntry.getEnrolledStudents(),
                originalEntry.getDayOfWeek(),
                originalEntry.getStartTime(),
                originalEntry.getEndTime(),
                originalEntry.getDate(),
                originalEntry.getRequestedRoomFeature(),
                optimizedRoom.getName(),
                optimizedRoom.getNormalCapacity(),
                optimizedRoomFeaturesText,
                optimizedRoom.getFeatures()
        );
    }
}
