package pt.iscte.ian;

import java.util.Set;
import java.util.stream.Collectors;

public class DatasetQualityAnalyzer {

    public DatasetQualityReport analyze(TimetablingDataset dataset) {
        Set<String> knownRoomNames = dataset.getRooms()
                .stream()
                .map(Room::getName)
                .collect(Collectors.toSet());

        Set<String> distinctRoomsUsed = dataset.getScheduleEntries()
                .stream()
                .map(ScheduleEntry::getRoomName)
                .filter(roomName -> roomName != null && !roomName.isBlank())
                .collect(Collectors.toSet());

        int entriesWithCapacityProblem = 0;
        int entriesWithoutRoom = 0;
        int entriesWithUnknownRoom = 0;

        for (ScheduleEntry entry : dataset.getScheduleEntries()) {
            String roomName = entry.getRoomName();

            if (roomName == null || roomName.isBlank()) {
                entriesWithoutRoom++;
                continue;
            }

            if (!knownRoomNames.contains(roomName)) {
                entriesWithUnknownRoom++;
            }

            if (entry.getEnrolledStudents() > entry.getRoomCapacity()) {
                entriesWithCapacityProblem++;
            }
        }

        return new DatasetQualityReport(
                dataset.getRooms().size(),
                dataset.getScheduleEntries().size(),
                distinctRoomsUsed.size(),
                entriesWithCapacityProblem,
                entriesWithoutRoom,
                entriesWithUnknownRoom
        );
    }
}