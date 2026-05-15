package pt.iscte.ian;

public class ScheduleEntry {

    private final String course;
    private final String curricularUnit;
    private final String shift;
    private final String classGroup;
    private final int enrolledStudents;
    private final String dayOfWeek;
    private final String startTime;
    private final String endTime;
    private final String date;
    private final String requestedRoomFeatures;
    private final String roomName;
    private final int roomCapacity;
    private final String realRoomFeatures;

    public ScheduleEntry(
            String course,
            String curricularUnit,
            String shift,
            String classGroup,
            int enrolledStudents,
            String dayOfWeek,
            String startTime,
            String endTime,
            String date,
            String requestedRoomFeatures,
            String roomName,
            int roomCapacity,
            String realRoomFeatures
    ) {
        this.course = course;
        this.curricularUnit = curricularUnit;
        this.shift = shift;
        this.classGroup = classGroup;
        this.enrolledStudents = enrolledStudents;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
        this.requestedRoomFeatures = requestedRoomFeatures;
        this.roomName = roomName;
        this.roomCapacity = roomCapacity;
        this.realRoomFeatures = realRoomFeatures;
    }

    public String getCourse() {
        return course;
    }

    public String getCurricularUnit() {
        return curricularUnit;
    }

    public String getShift() {
        return shift;
    }

    public String getClassGroup() {
        return classGroup;
    }

    public int getEnrolledStudents() {
        return enrolledStudents;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getDate() {
        return date;
    }

    public String getRequestedRoomFeatures() {
        return requestedRoomFeatures;
    }

    public String getRoomName() {
        return roomName;
    }

    public int getRoomCapacity() {
        return roomCapacity;
    }

    public String getRealRoomFeatures() {
        return realRoomFeatures;
    }

    @Override
    public String toString() {
        return "ScheduleEntry{" +
                "course='" + course + '\'' +
                ", curricularUnit='" + curricularUnit + '\'' +
                ", shift='" + shift + '\'' +
                ", classGroup='" + classGroup + '\'' +
                ", enrolledStudents=" + enrolledStudents +
                ", dayOfWeek='" + dayOfWeek + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", date='" + date + '\'' +
                ", roomName='" + roomName + '\'' +
                ", roomCapacity=" + roomCapacity +
                '}';
    }
}