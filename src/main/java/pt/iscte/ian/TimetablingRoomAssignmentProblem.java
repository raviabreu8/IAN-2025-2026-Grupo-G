package pt.iscte.ian;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;
import org.uma.jmetal.util.bounds.Bounds;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Representa o problema de atribuicao de salas no formato esperado pelo JMetal.
 *
 * <p>Cada variavel da solucao corresponde a uma aula problematica da instancia
 * de otimizacao. O valor da variavel escolhe uma sala dentro de uma lista reduzida
 * de salas candidatas para essa aula. A formulacao atual tem um unico objetivo:
 * minimizar a penalizacao total calculada pelo {@link TimetablingAssignmentEvaluator}.</p>
 */
@SuppressWarnings("serial")
public class TimetablingRoomAssignmentProblem implements Problem<IntegerSolution> {

    private static final int MAX_CANDIDATE_ROOMS_PER_ENTRY = 30;

    private final TimetablingOptimizationInstance instance;
    private final TimetablingAssignmentEvaluator evaluator;
    private final RoomFeatureMatcher featureMatcher;

    private final List<List<Integer>> candidateRoomIndexesByEntry;
    private final List<Bounds<Integer>> bounds;

    /**
     * Cria o problema JMetal a partir do dataset completo e da instancia simplificada.
     *
     * @param dataset dataset completo com salas e entradas de horario
     * @param instance instancia com as aulas a otimizar e as salas candidatas
     */
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

    /**
     * Devolve o numero de variaveis de decisao, ou seja, o numero de aulas a otimizar.
     *
     * @return numero de variaveis da solucao
     */
    @Override
    public int numberOfVariables() {
        return instance.getNumberOfVariables();
    }

    /**
     * Devolve o numero de objetivos da formulacao atual.
     *
     * @return um unico objetivo: minimizar a penalizacao total
     */
    @Override
    public int numberOfObjectives() {
        return 1;
    }

    /**
     * Devolve o numero de restricoes explicitas usadas pelo JMetal.
     *
     * @return zero, porque as restricoes sao tratadas atraves da funcao de penalizacao
     */
    @Override
    public int numberOfConstraints() {
        return 0;
    }

    /**
     * Devolve o nome do problema usado pelo JMetal.
     *
     * @return nome da formulacao de timetabling
     */
    @Override
    public String name() {
        return "TimetablingRoomAssignmentProblem";
    }

    /**
     * Avalia uma solucao JMetal e atribui-lhe a penalizacao total como objetivo.
     *
     * @param solution solucao inteira gerada pelo algoritmo de otimizacao
     * @return a mesma solucao, com o valor do objetivo atualizado
     */
    @Override
    public IntegerSolution evaluate(IntegerSolution solution) {
        TimetablingAssignment assignment = decodeSolution(solution);
        TimetablingAssignmentEvaluation evaluation = evaluator.evaluate(assignment);

        solution.objectives()[0] = evaluation.getTotalPenalty();

        return solution;
    }

    /**
     * Cria uma nova solucao inteira com os limites de escolha de sala por aula.
     *
     * @return solucao JMetal inicializada de acordo com os limites definidos
     */
    @Override
    public IntegerSolution createSolution() {
        return new DefaultIntegerSolution(
                bounds,
                numberOfObjectives(),
                numberOfConstraints()
        );
    }

    /**
     * Converte uma solucao JMetal, baseada em indices locais, numa atribuicao real de salas.
     *
     * <p>Internamente, cada variavel escolhe uma posicao na lista reduzida de salas
     * candidatas daquela aula. Este metodo traduz essa escolha para o indice real da
     * sala na lista global de salas candidatas.</p>
     *
     * @param solution solucao inteira produzida por um algoritmo JMetal
     * @return atribuicao de salas correspondente a solucao recebida
     */
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
