package pt.iscte.ian;

public class TimetablingSolutionEvaluation {

    private final int totalEntries;
    private final int invalidRoomAssignments;
    private final int capacityViolations;
    private final int totalCapacityShortage;
    private final int missingRoomAssignments;
    private final int unknownRoomAssignments;
    private final int roomTimeConflicts;
    private final int classGroupTimeConflicts;
    private final int featureMismatches;
    private final int totalUnusedCapacity;
    private final int totalPenalty;

    public TimetablingSolutionEvaluation(
            int totalEntries,
            int invalidRoomAssignments,
            int capacityViolations,
            int totalCapacityShortage,
            int missingRoomAssignments,
            int unknownRoomAssignments,
            int roomTimeConflicts,
            int classGroupTimeConflicts,
            int featureMismatches,
            int totalUnusedCapacity,
            int totalPenalty
    ) {
        this.totalEntries = totalEntries;
        this.invalidRoomAssignments = invalidRoomAssignments;
        this.capacityViolations = capacityViolations;
        this.totalCapacityShortage = totalCapacityShortage;
        this.missingRoomAssignments = missingRoomAssignments;
        this.unknownRoomAssignments = unknownRoomAssignments;
        this.roomTimeConflicts = roomTimeConflicts;
        this.classGroupTimeConflicts = classGroupTimeConflicts;
        this.featureMismatches = featureMismatches;
        this.totalUnusedCapacity = totalUnusedCapacity;
        this.totalPenalty = totalPenalty;
    }

    public int getTotalEntries() {
        return totalEntries;
    }

    public int getInvalidRoomAssignments() {
        return invalidRoomAssignments;
    }

    public int getCapacityViolations() {
        return capacityViolations;
    }

    public int getTotalCapacityShortage() {
        return totalCapacityShortage;
    }

    public int getMissingRoomAssignments() {
        return missingRoomAssignments;
    }

    public int getUnknownRoomAssignments() {
        return unknownRoomAssignments;
    }

    public int getRoomTimeConflicts() {
        return roomTimeConflicts;
    }

    public int getClassGroupTimeConflicts() {
        return classGroupTimeConflicts;
    }

    public int getFeatureMismatches() {
        return featureMismatches;
    }

    public int getTotalUnusedCapacity() {
        return totalUnusedCapacity;
    }

    public int getTotalPenalty() {
        return totalPenalty;
    }

    public double getCapacityViolationRate() {
        return totalEntries == 0 ? 0 : (double) capacityViolations / totalEntries;
    }

    public double getMissingRoomRate() {
        return totalEntries == 0 ? 0 : (double) missingRoomAssignments / totalEntries;
    }

    public double getUnknownRoomRate() {
        return totalEntries == 0 ? 0 : (double) unknownRoomAssignments / totalEntries;
    }

    public double getRoomTimeConflictRate() {
        return totalEntries == 0 ? 0 : (double) roomTimeConflicts / totalEntries;
    }

    public double getClassGroupTimeConflictRate() {
        return totalEntries == 0 ? 0 : (double) classGroupTimeConflicts / totalEntries;
    }

    public double getFeatureMismatchRate() {
        return totalEntries == 0 ? 0 : (double) featureMismatches / totalEntries;
    }

    @Override
    public String toString() {
        return "TimetablingSolutionEvaluation{" +
                "totalEntries=" + totalEntries +
                ", invalidRoomAssignments=" + invalidRoomAssignments +
                ", capacityViolations=" + capacityViolations +
                ", totalCapacityShortage=" + totalCapacityShortage +
                ", missingRoomAssignments=" + missingRoomAssignments +
                ", unknownRoomAssignments=" + unknownRoomAssignments +
                ", roomTimeConflicts=" + roomTimeConflicts +
                ", classGroupTimeConflicts=" + classGroupTimeConflicts +
                ", featureMismatches=" + featureMismatches +
                ", totalUnusedCapacity=" + totalUnusedCapacity +
                ", totalPenalty=" + totalPenalty +
                ", capacityViolationRate=" + getCapacityViolationRate() +
                ", missingRoomRate=" + getMissingRoomRate() +
                ", unknownRoomRate=" + getUnknownRoomRate() +
                ", roomTimeConflictRate=" + getRoomTimeConflictRate() +
                ", classGroupTimeConflictRate=" + getClassGroupTimeConflictRate() +
                ", featureMismatchRate=" + getFeatureMismatchRate() +
                '}';
    }
}
