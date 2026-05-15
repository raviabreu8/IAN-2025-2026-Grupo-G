package pt.iscte.ian;

public class Room {

    private final String building;
    private final String name;
    private final int normalCapacity;
    private final int examCapacity;

    public Room(String building, String name, int normalCapacity, int examCapacity) {
        this.building = building;
        this.name = name;
        this.normalCapacity = normalCapacity;
        this.examCapacity = examCapacity;
    }

    public String getBuilding() {
        return building;
    }

    public String getName() {
        return name;
    }

    public int getNormalCapacity() {
        return normalCapacity;
    }

    public int getExamCapacity() {
        return examCapacity;
    }

    @Override
    public String toString() {
        return "Room{" +
                "building='" + building + '\'' +
                ", name='" + name + '\'' +
                ", normalCapacity=" + normalCapacity +
                ", examCapacity=" + examCapacity +
                '}';
    }
}