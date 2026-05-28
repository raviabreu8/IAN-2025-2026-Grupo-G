package pt.iscte.ian;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class OptimizedScheduleExporter {

    private final RoomFeatureMatcher featureMatcher = new RoomFeatureMatcher();

    public void export(
            TimetablingDataset dataset,
            TimetablingOptimizationInstance instance,
            OptimizationResult result
    ) throws Exception {
        if (!result.hasBestPenaltyRoomIndexes()) {
            System.out.println("Não existe solução otimizada para aplicar ao horário completo.");
            return;
        }

        Path outputDirectory = Path.of("outputs");
        Files.createDirectories(outputDirectory);

        Path outputFile = outputDirectory.resolve("optimized_schedule_simplified.csv");

        Map<ScheduleEntry, Room> optimizedRoomByEntry =
                buildOptimizedRoomMap(instance, result);

        Map<String, Room> roomsByName = buildRoomsByName(dataset);

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(
                     writer,
                     CSVFormat.DEFAULT.builder()
                             .setDelimiter(';')
                             .setHeader(
                                     "entry_index",
                                     "assignment_source",
                                     "course",
                                     "curricular_unit",
                                     "shift",
                                     "class_group",
                                     "enrolled_students",
                                     "day_of_week",
                                     "date",
                                     "start_time",
                                     "end_time",
                                     "requested_room_feature",
                                     "original_room",
                                     "original_room_capacity",
                                     "assigned_room",
                                     "assigned_room_capacity",
                                     "capacity_difference",
                                     "feature_compatible",
                                     "assigned_room_features"
                             )
                             .build()
             )) {
            List<ScheduleEntry> entries = dataset.getScheduleEntries();

            for (int i = 0; i < entries.size(); i++) {
                ScheduleEntry entry = entries.get(i);

                Room optimizedRoom = optimizedRoomByEntry.get(entry);
                boolean optimized = optimizedRoom != null;

                Room assignedRoom = optimized
                        ? optimizedRoom
                        : roomsByName.get(entry.getRoomName());

                String assignmentSource = optimized ? result.getAlgorithm() : "ORIGINAL";

                String assignedRoomName = assignedRoom != null
                        ? assignedRoom.getName()
                        : entry.getRoomName();

                int assignedRoomCapacity = optimized
                        ? optimizedRoom.getNormalCapacity()
                        : entry.getRoomCapacity();

                int capacityDifference = assignedRoomCapacity - entry.getEnrolledStudents();

                boolean featureCompatible = isFeatureCompatible(entry, assignedRoom);

                String assignedRoomFeatures = assignedRoom != null
                        ? new TreeSet<>(assignedRoom.getFeatures()).stream()
                                .collect(Collectors.joining(", "))
                        : entry.getRealRoomFeaturesText();

                printer.printRecord(
                        i,
                        assignmentSource,
                        entry.getCourse(),
                        entry.getCurricularUnit(),
                        entry.getShift(),
                        entry.getClassGroup(),
                        entry.getEnrolledStudents(),
                        entry.getDayOfWeek(),
                        entry.getDate(),
                        entry.getStartTime(),
                        entry.getEndTime(),
                        entry.getRequestedRoomFeature(),
                        entry.getRoomName(),
                        entry.getRoomCapacity(),
                        assignedRoomName,
                        assignedRoomCapacity,
                        capacityDifference,
                        featureCompatible,
                        assignedRoomFeatures
                );
            }
        }

        System.out.println("Horário simplificado com otimização aplicado guardado em:");
        System.out.println(outputFile.toAbsolutePath());
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

    private Map<String, Room> buildRoomsByName(TimetablingDataset dataset) {
        Map<String, Room> roomsByName = new HashMap<>();

        for (Room room : dataset.getRooms()) {
            roomsByName.put(room.getName(), room);
        }

        return roomsByName;
    }

    private boolean isFeatureCompatible(ScheduleEntry entry, Room assignedRoom) {
        String requestedFeature = entry.getRequestedRoomFeature();

        if (requestedFeature == null || requestedFeature.isBlank()) {
            return true;
        }

        if (featureMatcher.isNoRoomNeeded(requestedFeature)) {
            return true;
        }

        if (assignedRoom == null) {
            return false;
        }

        return featureMatcher.matches(requestedFeature, assignedRoom);
    }
}
