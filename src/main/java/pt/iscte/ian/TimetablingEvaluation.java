package pt.iscte.ian;

public class TimetablingEvaluation {

    private final int totalEntries;
    private final int capacityViolations;
    private final int missingRoomAssignments;
    private final int unknownRoomAssignments;
    private final int roomTimeConflicts;
    private final int totalPenalty;

    public TimetablingEvaluation(
            int totalEntries,
            int capacityViolations,
            int missingRoomAssignments,
            int unknownRoomAssignments,
            int roomTimeConflicts,
            int totalPenalty
    ) {
        this.totalEntries = totalEntries;
        this.capacityViolations = capacityViolations;
        this.missingRoomAssignments = missingRoomAssignments;
        this.unknownRoomAssignments = unknownRoomAssignments;
        this.roomTimeConflicts = roomTimeConflicts;
        this.totalPenalty = totalPenalty;
    }

    public int getTotalEntries() {
        return totalEntries;
    }

    public int getCapacityViolations() {
        return capacityViolations;
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

    @Override
    public String toString() {
        return "TimetablingEvaluation{" +
                "totalEntries=" + totalEntries +
                ", capacityViolations=" + capacityViolations +
                ", missingRoomAssignments=" + missingRoomAssignments +
                ", unknownRoomAssignments=" + unknownRoomAssignments +
                ", roomTimeConflicts=" + roomTimeConflicts +
                ", totalPenalty=" + totalPenalty +
                ", capacityViolationRate=" + getCapacityViolationRate() +
                ", missingRoomRate=" + getMissingRoomRate() +
                ", unknownRoomRate=" + getUnknownRoomRate() +
                '}';
    }
}