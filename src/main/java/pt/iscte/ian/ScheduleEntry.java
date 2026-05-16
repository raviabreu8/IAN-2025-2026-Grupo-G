package pt.iscte.ian;

import java.util.Set;
import java.util.TreeSet;

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

    private final String requestedRoomFeature;

    private final String roomName;
    private final int roomCapacity;

    private final String realRoomFeaturesText;
    private final Set<String> realRoomFeatures;

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
            String requestedRoomFeature,
            String roomName,
            int roomCapacity,
            String realRoomFeaturesText,
            Set<String> realRoomFeatures
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
        this.requestedRoomFeature = requestedRoomFeature;
        this.roomName = roomName;
        this.roomCapacity = roomCapacity;
        this.realRoomFeaturesText = realRoomFeaturesText;
        this.realRoomFeatures = Set.copyOf(realRoomFeatures);
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

    public String getRequestedRoomFeature() {
        return requestedRoomFeature;
    }

    public String getRequestedRoomFeatures() {
        return requestedRoomFeature;
    }

    public String getRoomName() {
        return roomName;
    }

    public int getRoomCapacity() {
        return roomCapacity;
    }

    public String getRealRoomFeaturesText() {
        return realRoomFeaturesText;
    }

    public String getRealRoomFeatures() {
        return realRoomFeaturesText;
    }

    public Set<String> getRealRoomFeaturesSet() {
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
                ", requestedRoomFeature='" + requestedRoomFeature + '\'' +
                ", roomName='" + roomName + '\'' +
                ", roomCapacity=" + roomCapacity +
                ", realRoomFeatures=" + new TreeSet<>(realRoomFeatures) +
                '}';
    }
}
