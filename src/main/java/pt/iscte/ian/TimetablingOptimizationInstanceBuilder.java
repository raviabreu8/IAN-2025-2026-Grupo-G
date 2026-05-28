package pt.iscte.ian;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Constroi a instancia simplificada de otimizacao a partir do dataset completo.
 *
 * <p>Esta classe seleciona as aulas consideradas problematicas, ordena-as por
 * prioridade e limita o tamanho da instancia de acordo com a configuracao da
 * aplicacao. Tambem prepara a lista de salas candidatas que podem ser escolhidas
 * pelos algoritmos de otimizacao.</p>
 */
public class TimetablingOptimizationInstanceBuilder {

    private final RoomFeatureMatcher featureMatcher = new RoomFeatureMatcher();

    /**
     * Cria uma instancia de otimizacao com as entradas mais problematicas do horario.
     *
     * @param dataset dataset completo com salas e entradas de horario
     * @param maxEntriesForOptimization numero maximo de entradas problematicas a incluir na instancia
     * @return instancia com as entradas a otimizar e as salas candidatas
     */
    public TimetablingOptimizationInstance build(
            TimetablingDataset dataset,
            int maxEntriesForOptimization
    ) {
        Map<String, Room> roomsByName = dataset.getRooms()
                .stream()
                .collect(Collectors.toMap(
                        Room::getName,
                        room -> room,
                        (first, second) -> first
                ));

        List<ScheduleEntry> problematicEntries = dataset.getScheduleEntries()
                .stream()
                .filter(entry -> isProblematic(entry, roomsByName))
                .sorted(Comparator.comparingInt(
                        (ScheduleEntry entry) -> problemPriority(entry, roomsByName)
                ).reversed())
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

    private boolean isProblematic(
            ScheduleEntry entry,
            Map<String, Room> roomsByName
    ) {
        if (featureMatcher.isNoRoomNeeded(entry.getRequestedRoomFeature())) {
            return false;
        }

        return problemPriority(entry, roomsByName) > 0;
    }

    private int problemPriority(
            ScheduleEntry entry,
            Map<String, Room> roomsByName
    ) {
        int priority = 0;

        String roomName = entry.getRoomName();

        boolean missingRoom = roomName == null || roomName.isBlank();

        if (missingRoom) {
            priority += TimetablingPenaltyWeights.MISSING_ROOM_ASSIGNMENT;
            priority += entry.getEnrolledStudents();
            return priority;
        }

        Room room = roomsByName.get(roomName);

        boolean unknownRoom = room == null;

        if (unknownRoom) {
            priority += TimetablingPenaltyWeights.UNKNOWN_ROOM_ASSIGNMENT;
            priority += entry.getEnrolledStudents();
            return priority;
        }

        if (entry.getRoomCapacity() > 0) {
            int capacityDifference = entry.getRoomCapacity() - entry.getEnrolledStudents();

            if (capacityDifference < 0) {
                priority += TimetablingPenaltyWeights.CAPACITY_VIOLATION;
                priority += Math.abs(capacityDifference) * TimetablingPenaltyWeights.CAPACITY_SHORTAGE_PER_SEAT;
            }
        }

        if (hasFeatureMismatch(entry, room)) {
            priority += TimetablingPenaltyWeights.FEATURE_MISMATCH;
        }

        if (priority > 0) {
            priority += entry.getEnrolledStudents();
        }

        return priority;
    }

    private boolean hasFeatureMismatch(
            ScheduleEntry entry,
            Room room
    ) {
        String requestedFeature = entry.getRequestedRoomFeature();

        if (requestedFeature == null || requestedFeature.isBlank()) {
            return false;
        }

        if (featureMatcher.isNoRoomNeeded(requestedFeature)) {
            return false;
        }

        return !featureMatcher.matches(requestedFeature, room);
    }
}
