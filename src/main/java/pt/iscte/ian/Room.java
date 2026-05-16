package pt.iscte.ian;

import java.util.Set;
import java.util.TreeSet;

public class Room {

    private final String building;
    private final String name;
    private final int normalCapacity;
    private final int examCapacity;
    private final Set<String> features;

    public Room(
            String building,
            String name,
            int normalCapacity,
            int examCapacity,
            Set<String> features
    ) {
        this.building = building;
        this.name = name;
        this.normalCapacity = normalCapacity;
        this.examCapacity = examCapacity;
        this.features = Set.copyOf(features);
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

    public Set<String> getFeatures() {
        return features;
    }

    public boolean hasFeature(String feature) {
        return features.contains(feature);
    }

    @Override
    public String toString() {
        return "Room{" +
                "building='" + building + '\'' +
                ", name='" + name + '\'' +
                ", normalCapacity=" + normalCapacity +
                ", examCapacity=" + examCapacity +
                ", features=" + new TreeSet<>(features) +
                '}';
    }
}
