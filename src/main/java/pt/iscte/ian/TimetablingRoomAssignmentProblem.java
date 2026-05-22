package pt.iscte.ian;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;
import org.uma.jmetal.util.bounds.Bounds;

import java.util.ArrayList;
import java.util.List;

public class TimetablingRoomAssignmentProblem implements Problem<IntegerSolution> {

    private static final long serialVersionUID = 1L;

    private final TimetablingOptimizationInstance instance;
    private final TimetablingAssignmentEvaluator evaluator;
    private final List<Bounds<Integer>> bounds;

    public TimetablingRoomAssignmentProblem(
            TimetablingDataset dataset,
            TimetablingOptimizationInstance instance
    ) {
        this.instance = instance;
        this.evaluator = new TimetablingAssignmentEvaluator(dataset, instance);
        this.bounds = createBounds(instance);
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
        int[] roomIndexes = new int[numberOfVariables()];

        for (int i = 0; i < numberOfVariables(); i++) {
            roomIndexes[i] = solution.variables().get(i);
        }

        TimetablingAssignment assignment = new TimetablingAssignment(roomIndexes);

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

    private List<Bounds<Integer>> createBounds(TimetablingOptimizationInstance instance) {
        List<Bounds<Integer>> boundsList = new ArrayList<>();

        int lowerBound = 0;
        int upperBound = instance.getNumberOfCandidateRooms() - 1;

        for (int i = 0; i < instance.getNumberOfVariables(); i++) {
            boundsList.add(Bounds.create(lowerBound, upperBound));
        }

        return boundsList;
    }
}
