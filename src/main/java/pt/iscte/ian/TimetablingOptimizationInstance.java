package pt.iscte.ian;

import java.util.List;

public class TimetablingOptimizationInstance {

    private final List<ScheduleEntry> entriesToOptimize;
    private final List<Room> candidateRooms;

    public TimetablingOptimizationInstance(
            List<ScheduleEntry> entriesToOptimize,
            List<Room> candidateRooms
    ) {
        this.entriesToOptimize = entriesToOptimize;
        this.candidateRooms = candidateRooms;
    }

    public List<ScheduleEntry> getEntriesToOptimize() {
        return entriesToOptimize;
    }

    public List<Room> getCandidateRooms() {
        return candidateRooms;
    }

    public int getNumberOfVariables() {
        return entriesToOptimize.size();
    }

    public int getNumberOfCandidateRooms() {
        return candidateRooms.size();
    }

    @Override
    public String toString() {
        return "TimetablingOptimizationInstance{" +
                "entriesToOptimize=" + entriesToOptimize.size() +
                ", candidateRooms=" + candidateRooms.size() +
                '}';
    }
}