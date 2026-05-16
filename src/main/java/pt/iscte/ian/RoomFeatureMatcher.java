package pt.iscte.ian;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RoomFeatureMatcher {

    private final Map<String, Set<String>> compatibleFeaturesByRequestedFeature;

    public RoomFeatureMatcher() {
        this.compatibleFeaturesByRequestedFeature = buildCompatibilityMap();
    }

    public boolean matches(ScheduleEntry entry, Room room) {
        if (entry == null) {
            return false;
        }

        return matches(entry.getRequestedRoomFeature(), room);
    }

    public boolean matches(String requestedFeature, Room room) {
        String normalizedRequestedFeature = normalizeFeatureName(requestedFeature);

        if (normalizedRequestedFeature.isBlank()) {
            return true;
        }

        if (isNoRoomNeeded(normalizedRequestedFeature)) {
            return true;
        }

        if (room == null) {
            return false;
        }

        Set<String> normalizedRoomFeatures = normalizeFeatures(room.getFeatures());
        Set<String> compatibleFeatures = getCompatibleFeatures(normalizedRequestedFeature);

        for (String compatibleFeature : compatibleFeatures) {
            if (normalizedRoomFeatures.contains(compatibleFeature)) {
                return true;
            }
        }

        return false;
    }

    public Set<String> getCompatibleFeatures(String requestedFeature) {
        String normalizedRequestedFeature = normalizeFeatureName(requestedFeature);

        if (compatibleFeaturesByRequestedFeature.containsKey(normalizedRequestedFeature)) {
            return compatibleFeaturesByRequestedFeature.get(normalizedRequestedFeature);
        }

        return Set.of(normalizedRequestedFeature);
    }

    private Map<String, Set<String>> buildCompatibilityMap() {
        Map<String, Set<String>> map = new HashMap<>();

        map.put(
                "sala aulas mestrado",
                Set.of(
                        "sala aulas mestrado",
                        "sala aulas mestrado plus"
                )
        );

        map.put(
                "sala aulas mestrado plus",
                Set.of(
                        "sala aulas mestrado plus"
                )
        );

        map.put(
                "sala de aulas normal",
                Set.of(
                        "sala de aulas normal",
                        "anfiteatro aulas",
                        "sala aulas mestrado",
                        "sala aulas mestrado plus"
                )
        );

        map.put(
                "sala/anfiteatro aulas",
                Set.of(
                        "sala de aulas normal",
                        "anfiteatro aulas",
                        "sala aulas mestrado",
                        "sala aulas mestrado plus"
                )
        );

        map.put(
                "anfiteatro aulas",
                Set.of(
                        "anfiteatro aulas"
                )
        );

        map.put(
                "byod (bring your own device)",
                Set.of(
                        "byod (bring your own device)"
                )
        );

        map.put(
                "videoconferencia",
                Set.of(
                        "videoconferencia"
                )
        );

        map.put(
                "lab ista",
                Set.of(
                        "laboratorio de informatica",
                        "laboratorio de arquitectura de computadores i",
                        "laboratorio de arquitectura de computadores ii",
                        "laboratorio de redes de computadores i",
                        "laboratorio de redes de computadores ii",
                        "laboratorio de telecomunicacoes",
                        "laboratorio de electronica",
                        "laboratorio de bases de engenharia"
                )
        );

        map.put(
                "laboratorio de informatica",
                Set.of(
                        "laboratorio de informatica"
                )
        );

        map.put(
                "laboratorio de arquitectura de computadores i",
                Set.of(
                        "laboratorio de arquitectura de computadores i"
                )
        );

        map.put(
                "laboratorio de arquitectura de computadores ii",
                Set.of(
                        "laboratorio de arquitectura de computadores ii"
                )
        );

        map.put(
                "laboratorio de electronica",
                Set.of(
                        "laboratorio de electronica"
                )
        );

        map.put(
                "laboratorio de jornalismo",
                Set.of(
                        "laboratorio de jornalismo"
                )
        );

        map.put(
                "laboratorio de redes de computadores i",
                Set.of(
                        "laboratorio de redes de computadores i"
                )
        );

        map.put(
                "laboratorio de redes de computadores ii",
                Set.of(
                        "laboratorio de redes de computadores ii"
                )
        );

        map.put(
                "laboratorio de telecomunicacoes",
                Set.of(
                        "laboratorio de telecomunicacoes"
                )
        );

        map.put(
                "arq 1",
                Set.of("arq 1")
        );

        map.put(
                "arq 2",
                Set.of("arq 2")
        );

        map.put(
                "arq 3",
                Set.of("arq 3")
        );

        map.put(
                "arq 4",
                Set.of("arq 4")
        );

        map.put(
                "arq 5",
                Set.of("arq 5")
        );

        map.put(
                "arq 6",
                Set.of("arq 6")
        );

        return map;
    }

    public boolean isNoRoomNeeded(String requestedFeature) {
        String normalizedRequestedFeature = normalizeFeatureName(requestedFeature);
        return normalizedRequestedFeature.equals("nao necessita de sala");
    }

    private Set<String> normalizeFeatures(Set<String> features) {
        Set<String> normalizedFeatures = new HashSet<>();

        for (String feature : features) {
            String normalizedFeature = normalizeFeatureName(feature);

            if (!normalizedFeature.isBlank()) {
                normalizedFeatures.add(normalizedFeature);
            }
        }

        return normalizedFeatures;
    }

    private String normalizeFeatureName(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String normalized = value.trim()
                .replace("_", " ")
                .replaceAll("\\s+", " ")
                .toLowerCase();

        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return normalized;
    }
}
