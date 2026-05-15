package pt.iscte.ian;

import java.util.List;

public class TimetablingDataset {

    private final List<Room> rooms;
    private final List<ScheduleEntry> scheduleEntries;

    public TimetablingDataset(List<Room> rooms, List<ScheduleEntry> scheduleEntries) {
        this.rooms = rooms;
        this.scheduleEntries = scheduleEntries;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public List<ScheduleEntry> getScheduleEntries() {
        return scheduleEntries;
    }
}