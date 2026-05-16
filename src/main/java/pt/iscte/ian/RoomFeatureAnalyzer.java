package pt.iscte.ian;

import java.util.Map;
import java.util.stream.Collectors;

public class RoomFeatureAnalyzer {

    private final RoomFeatureMatcher matcher = new RoomFeatureMatcher();

    public RoomFeatureAnalysisReport analyze(TimetablingDataset dataset) {
        Map<String, Room> roomsByName = dataset.getRooms()
                .stream()
                .collect(Collectors.toMap(
                        Room::getName,
                        room -> room,
                        (first, second) -> first
                ));

        int entriesWithRequestedFeature = 0;
        int noRoomNeededEntries = 0;
        int entriesEvaluatedWithKnownRoom = 0;
        int featureMatches = 0;
        int featureMismatches = 0;
        int entriesSkippedWithoutRoomOrUnknownRoom = 0;

        for (ScheduleEntry entry : dataset.getScheduleEntries()) {
            String requestedFeature = entry.getRequestedRoomFeature();

            if (requestedFeature == null || requestedFeature.isBlank()) {
                continue;
            }

            entriesWithRequestedFeature++;

            if (matcher.isNoRoomNeeded(requestedFeature)) {
                noRoomNeededEntries++;
                continue;
            }

            String roomName = entry.getRoomName();

            if (roomName == null || roomName.isBlank()) {
                entriesSkippedWithoutRoomOrUnknownRoom++;
                continue;
            }

            Room assignedRoom = roomsByName.get(roomName);

            if (assignedRoom == null) {
                entriesSkippedWithoutRoomOrUnknownRoom++;
                continue;
            }

            entriesEvaluatedWithKnownRoom++;

            if (matcher.matches(requestedFeature, assignedRoom)) {
                featureMatches++;
            } else {
                featureMismatches++;
            }
        }

        return new RoomFeatureAnalysisReport(
                entriesWithRequestedFeature,
                noRoomNeededEntries,
                entriesEvaluatedWithKnownRoom,
                featureMatches,
                featureMismatches,
                entriesSkippedWithoutRoomOrUnknownRoom
        );
    }
}
