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

/**
 * Avalia uma atribuicao de salas para as entradas selecionadas para otimizacao.
 *
 * <p>Esta classe calcula as metricas usadas como penalizacao no problema:
 * atribuicoes invalidas, violacoes de capacidade, lugares em falta, conflitos
 * temporais de sala, incompatibilidades de caracteristicas e capacidade
 * desperdicada.</p>
 */
public class TimetablingAssignmentEvaluator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final List<ScheduleEntry> entriesToOptimize;
    private final List<Room> candidateRooms;
    private final Map<String, List<ScheduleInterval>> fixedRoomOccupancy;
    private final RoomFeatureMatcher featureMatcher = new RoomFeatureMatcher();

    /**
     * Cria um avaliador para uma instancia de otimizacao.
     *
     * <p>O avaliador guarda tambem a ocupacao fixa das salas no horario completo,
     * excluindo as entradas que vao ser otimizadas, para conseguir detetar conflitos
     * entre a nova atribuicao e o horario que permanece inalterado.</p>
     *
     * @param dataset dataset completo de salas e horarios
     * @param instance instancia com as entradas a otimizar e as salas candidatas
     */
    public TimetablingAssignmentEvaluator(
            TimetablingDataset dataset,
            TimetablingOptimizationInstance instance
    ) {
        this.entriesToOptimize = instance.getEntriesToOptimize();
        this.candidateRooms = instance.getCandidateRooms();
        this.fixedRoomOccupancy = buildFixedRoomOccupancy(dataset, entriesToOptimize);
    }

    /**
     * Avalia uma atribuicao candidata e calcula a respetiva penalizacao total.
     *
     * @param assignment atribuicao de indices de salas para as entradas otimizadas
     * @return avaliacao detalhada da atribuicao recebida
     */
    public TimetablingAssignmentEvaluation evaluate(TimetablingAssignment assignment) {
        Map<String, List<ScheduleInterval>> assignedRoomOccupancy = new HashMap<>();

        int assignedEntries = assignment.size();
        int invalidRoomAssignments = 0;
        int capacityViolations = 0;
        int totalCapacityShortage = 0;
        int roomTimeConflicts = 0;
        int featureMismatches = 0;
        int totalUnusedCapacity = 0;

        for (int i = 0; i < assignment.size(); i++) {
            ScheduleEntry entry = entriesToOptimize.get(i);
            int roomIndex = assignment.getRoomIndexForEntry(i);

            if (roomIndex < 0 || roomIndex >= candidateRooms.size()) {
                invalidRoomAssignments++;
                continue;
            }

            Room selectedRoom = candidateRooms.get(roomIndex);
            String requestedFeature = entry.getRequestedRoomFeature();

            if (requestedFeature != null
                    && !requestedFeature.isBlank()
                    && !featureMatcher.isNoRoomNeeded(requestedFeature)
                    && !featureMatcher.matches(requestedFeature, selectedRoom)) {
                featureMismatches++;
            }

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
                invalidRoomAssignments * TimetablingPenaltyWeights.INVALID_ROOM_ASSIGNMENT +
                capacityViolations * TimetablingPenaltyWeights.CAPACITY_VIOLATION +
                totalCapacityShortage * TimetablingPenaltyWeights.CAPACITY_SHORTAGE_PER_SEAT +
                roomTimeConflicts * TimetablingPenaltyWeights.ROOM_TIME_CONFLICT +
                featureMismatches * TimetablingPenaltyWeights.FEATURE_MISMATCH +
                totalUnusedCapacity * TimetablingPenaltyWeights.UNUSED_CAPACITY_PER_SEAT;

        return new TimetablingAssignmentEvaluation(
                assignedEntries,
                invalidRoomAssignments,
                capacityViolations,
                totalCapacityShortage,
                roomTimeConflicts,
                featureMismatches,
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
