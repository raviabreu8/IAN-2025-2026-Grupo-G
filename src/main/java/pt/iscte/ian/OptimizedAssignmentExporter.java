package pt.iscte.ian;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class OptimizedAssignmentExporter {

    public void export(
            TimetablingOptimizationInstance instance,
            OptimizationResult result
    ) throws Exception {
        if (!result.hasBestPenaltyRoomIndexes()) {
            System.out.println("Não existem atribuições otimizadas para exportar.");
            return;
        }

        Path outputDirectory = Path.of("outputs");
        Files.createDirectories(outputDirectory);

        Path outputFile = outputDirectory.resolve("optimized_assignments.csv");

        List<ScheduleEntry> entries = instance.getEntriesToOptimize();
        List<Room> rooms = instance.getCandidateRooms();
        int[] roomIndexes = result.getBestPenaltyRoomIndexes();

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(
                     writer,
                     CSVFormat.DEFAULT.builder()
                             .setDelimiter(';')
                             .setHeader(
                                     "entry_index",
                                     "course",
                                     "curricular_unit",
                                     "shift",
                                     "class_group",
                                     "date",
                                     "start_time",
                                     "end_time",
                                     "enrolled_students",
                                     "original_room",
                                     "selected_room",
                                     "selected_room_capacity",
                                     "unused_capacity"
                             )
                             .build()
             )) {

            int limit = Math.min(entries.size(), roomIndexes.length);

            for (int i = 0; i < limit; i++) {
                ScheduleEntry entry = entries.get(i);
                int roomIndex = roomIndexes[i];

                if (roomIndex < 0 || roomIndex >= rooms.size()) {
                    printer.printRecord(
                            i,
                            entry.getCourse(),
                            entry.getCurricularUnit(),
                            entry.getShift(),
                            entry.getClassGroup(),
                            entry.getDate(),
                            entry.getStartTime(),
                            entry.getEndTime(),
                            entry.getEnrolledStudents(),
                            entry.getRoomName(),
                            "INVALID_ROOM_INDEX",
                            "",
                            ""
                    );
                    continue;
                }

                Room selectedRoom = rooms.get(roomIndex);
                int unusedCapacity = selectedRoom.getNormalCapacity() - entry.getEnrolledStudents();

                printer.printRecord(
                        i,
                        entry.getCourse(),
                        entry.getCurricularUnit(),
                        entry.getShift(),
                        entry.getClassGroup(),
                        entry.getDate(),
                        entry.getStartTime(),
                        entry.getEndTime(),
                        entry.getEnrolledStudents(),
                        entry.getRoomName(),
                        selectedRoom.getName(),
                        selectedRoom.getNormalCapacity(),
                        unusedCapacity
                );
            }
        }

        System.out.println("Atribuições otimizadas guardadas em:");
        System.out.println(outputFile.toAbsolutePath());
    }
}
