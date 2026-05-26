package pt.iscte.ian;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;
import org.uma.jmetal.util.bounds.Bounds;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("serial")
public class TimetablingRoomAssignmentProblem implements Problem<IntegerSolution> {

    private static final int MAX_CANDIDATE_ROOMS_PER_ENTRY = 30;

    private final TimetablingOptimizationInstance instance;
    private final TimetablingAssignmentEvaluator evaluator;
    private final RoomFeatureMatcher featureMatcher;

    private final List<List<Integer>> candidateRoomIndexesByEntry;
    private final List<Bounds<Integer>> bounds;

    public TimetablingRoomAssignmentProblem(
            TimetablingDataset dataset,
            TimetablingOptimizationInstance instance
    ) {
        this.instance = instance;
        this.evaluator = new TimetablingAssignmentEvaluator(dataset, instance);
        this.featureMatcher = new RoomFeatureMatcher();

        this.candidateRoomIndexesByEntry = createCandidateRoomIndexesByEntry(instance);
        this.bounds = createBounds();

        System.out.println("Listas de salas candidatas por aula criadas:");
        System.out.println("Máximo de salas candidatas por aula: " + MAX_CANDIDATE_ROOMS_PER_ENTRY);
        System.out.println("Mínimo encontrado: " + getMinimumCandidateCount());
        System.out.println("Máximo encontrado: " + getMaximumCandidateCount());
    }

    @Override
    public int numberOfVariables() {
        return instance.getNumberOfVariables();
    }

    @Override
    public int numberOfObjectives() {
        return 1;
    }

    @Override
    public int numberOfConstraints() {
        return 0;
    }

    @Override
    public String name() {
        return "TimetablingRoomAssignmentProblem";
    }

    @Override
    public IntegerSolution evaluate(IntegerSolution solution) {
        TimetablingAssignment assignment = decodeSolution(solution);
        TimetablingAssignmentEvaluation evaluation = evaluator.evaluate(assignment);

        solution.objectives()[0] = evaluation.getTotalPenalty();

        return solution;
    }

    @Override
    public IntegerSolution createSolution() {
        return new DefaultIntegerSolution(
                bounds,
                numberOfObjectives(),
                numberOfConstraints()
        );
    }

    public TimetablingAssignment decodeSolution(IntegerSolution solution) {
        int[] realRoomIndexes = new int[numberOfVariables()];

        for (int i = 0; i < numberOfVariables(); i++) {
            int localChoice = solution.variables().get(i);
            List<Integer> candidateRoomIndexes = candidateRoomIndexesByEntry.get(i);

            if (localChoice < 0 || localChoice >= candidateRoomIndexes.size()) {
                realRoomIndexes[i] = candidateRoomIndexes.get(0);
            } else {
                realRoomIndexes[i] = candidateRoomIndexes.get(localChoice);
            }
        }

        return new TimetablingAssignment(realRoomIndexes);
    }

    private List<List<Integer>> createCandidateRoomIndexesByEntry(
            TimetablingOptimizationInstance instance
    ) {
        List<List<Integer>> result = new ArrayList<>();

        List<ScheduleEntry> entries = instance.getEntriesToOptimize();
        List<Room> rooms = instance.getCandidateRooms();

        for (ScheduleEntry entry : entries) {
            result.add(selectBestCandidateRoomIndexes(entry, rooms));
        }

        return List.copyOf(result);
    }

    private List<Integer> selectBestCandidateRoomIndexes(
            ScheduleEntry entry,
            List<Room> rooms
    ) {
        List<RoomCandidate> candidates = new ArrayList<>();

        for (int roomIndex = 0; roomIndex < rooms.size(); roomIndex++) {
            Room room = rooms.get(roomIndex);

            int localPenalty = estimateLocalPenalty(entry, room);

            candidates.add(new RoomCandidate(roomIndex, localPenalty));
        }

        candidates.sort(
                Comparator.comparingInt(RoomCandidate::localPenalty)
                        .thenComparingInt(candidate -> rooms.get(candidate.roomIndex()).getNormalCapacity())
                        .thenComparingInt(RoomCandidate::roomIndex)
        );

        int limit = Math.min(MAX_CANDIDATE_ROOMS_PER_ENTRY, candidates.size());

        List<Integer> selectedRoomIndexes = new ArrayList<>();

        for (int i = 0; i < limit; i++) {
            selectedRoomIndexes.add(candidates.get(i).roomIndex());
        }

        if (selectedRoomIndexes.isEmpty()) {
            selectedRoomIndexes.add(0);
        }

        return List.copyOf(selectedRoomIndexes);
    }

    private int estimateLocalPenalty(ScheduleEntry entry, Room room) {
        int penalty = 0;

        String requestedFeature = entry.getRequestedRoomFeature();

        if (requestedFeature != null
                && !requestedFeature.isBlank()
                && !featureMatcher.isNoRoomNeeded(requestedFeature)
                && !featureMatcher.matches(requestedFeature, room)) {
            penalty += TimetablingPenaltyWeights.FEATURE_MISMATCH;
        }

        int capacityDifference = room.getNormalCapacity() - entry.getEnrolledStudents();

        if (capacityDifference < 0) {
            penalty += TimetablingPenaltyWeights.CAPACITY_VIOLATION;
            penalty += Math.abs(capacityDifference) * TimetablingPenaltyWeights.CAPACITY_SHORTAGE_PER_SEAT;
        } else {
            penalty += capacityDifference * TimetablingPenaltyWeights.UNUSED_CAPACITY_PER_SEAT;
        }

        return penalty;
    }

    private List<Bounds<Integer>> createBounds() {
        List<Bounds<Integer>> boundsList = new ArrayList<>();

        for (List<Integer> candidateRoomIndexes : candidateRoomIndexesByEntry) {
            boundsList.add(
                    Bounds.create(
                            0,
                            candidateRoomIndexes.size() - 1
                    )
            );
        }

        return List.copyOf(boundsList);
    }

    private int getMinimumCandidateCount() {
        return candidateRoomIndexesByEntry.stream()
                .mapToInt(List::size)
                .min()
                .orElse(0);
    }

    private int getMaximumCandidateCount() {
        return candidateRoomIndexesByEntry.stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);
    }

    private record RoomCandidate(
            int roomIndex,
            int localPenalty
    ) {
    }
}
