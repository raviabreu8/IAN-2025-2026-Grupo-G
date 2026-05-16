package pt.iscte.ian;

public class TimetablingAssignmentEvaluation {

    private final int assignedEntries;
    private final int invalidRoomAssignments;
    private final int capacityViolations;
    private final int totalCapacityShortage;
    private final int roomTimeConflicts;
    private final int totalUnusedCapacity;
    private final int totalPenalty;

    public TimetablingAssignmentEvaluation(
            int assignedEntries,
            int invalidRoomAssignments,
            int capacityViolations,
            int totalCapacityShortage,
            int roomTimeConflicts,
            int totalUnusedCapacity,
            int totalPenalty
    ) {
        this.assignedEntries = assignedEntries;
        this.invalidRoomAssignments = invalidRoomAssignments;
        this.capacityViolations = capacityViolations;
        this.totalCapacityShortage = totalCapacityShortage;
        this.roomTimeConflicts = roomTimeConflicts;
        this.totalUnusedCapacity = totalUnusedCapacity;
        this.totalPenalty = totalPenalty;
    }

    public int getAssignedEntries() {
        return assignedEntries;
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

    public int getRoomTimeConflicts() {
        return roomTimeConflicts;
    }

    public int getTotalUnusedCapacity() {
        return totalUnusedCapacity;
    }

    public int getTotalPenalty() {
        return totalPenalty;
    }

    @Override
    public String toString() {
        return "TimetablingAssignmentEvaluation{" +
                "assignedEntries=" + assignedEntries +
                ", invalidRoomAssignments=" + invalidRoomAssignments +
                ", capacityViolations=" + capacityViolations +
                ", totalCapacityShortage=" + totalCapacityShortage +
                ", roomTimeConflicts=" + roomTimeConflicts +
                ", totalUnusedCapacity=" + totalUnusedCapacity +
                ", totalPenalty=" + totalPenalty +
                '}';
    }
}
