package pt.iscte.ian;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DatasetLoader {

    public List<Room> loadRooms() throws Exception {
        Path filePath = Path.of("data", "input", "Caracterizacao das salas.csv");

        if (!Files.exists(filePath)) {
            throw new RuntimeException("Ficheiro de salas não encontrado: " + filePath.toAbsolutePath());
        }

        List<Room> rooms = new ArrayList<>();

        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT
                     .builder()
                     .setDelimiter(';')
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {

            for (CSVRecord record : parser) {
                String building = getValue(record, "Edifício");
                String name = normalizeRoomName(getValue(record, "Nome_sala"));
                int normalCapacity = parseInt(getValue(record, "Capacidade_Normal"));
                int examCapacity = parseInt(getValue(record, "Capacidade_Exame"));
                Set<String> features = extractRoomFeatures(record);

                rooms.add(new Room(
                        building,
                        name,
                        normalCapacity,
                        examCapacity,
                        features
                ));
            }
        }

        return rooms;
    }

    public List<ScheduleEntry> loadScheduleEntries() throws Exception {
        Path filePath = Path.of("data", "input", "Horários 1º sem 2022-23.csv");

        if (!Files.exists(filePath)) {
            throw new RuntimeException("Ficheiro de horários não encontrado: " + filePath.toAbsolutePath());
        }

        List<ScheduleEntry> entries = new ArrayList<>();

        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT
                     .builder()
                     .setDelimiter(';')
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {

            for (CSVRecord record : parser) {
                String course = getFirstAvailableValue(record, "Curso", "?Curso", "\uFEFFCurso");
                String curricularUnit = getValue(record, "Unidade de execução");
                String shift = getValue(record, "Turno");
                String classGroup = getValue(record, "Turma");
                int enrolledStudents = parseInt(getValue(record, "Inscritos no turno"));
                String dayOfWeek = getValue(record, "Dia da Semana");
                String startTime = getValue(record, "Início");
                String endTime = getValue(record, "Fim");
                String date = getValue(record, "Dia");
                String requestedRoomFeature = normalizeFeatureName(
                        getValue(record, "Características da sala pedida para a aula")
                );
                String roomName = normalizeRoomName(getValue(record, "Sala da aula"));
                int roomCapacity = parseInt(getValue(record, "Lotação"));
                String realRoomFeaturesText = getValue(record, "Características reais da sala");
                Set<String> realRoomFeatures = extractFeatureList(realRoomFeaturesText);

                entries.add(new ScheduleEntry(
                        course,
                        curricularUnit,
                        shift,
                        classGroup,
                        enrolledStudents,
                        dayOfWeek,
                        startTime,
                        endTime,
                        date,
                        requestedRoomFeature,
                        roomName,
                        roomCapacity,
                        realRoomFeaturesText,
                        realRoomFeatures
                ));
            }
        }

        return entries;
    }

    private String getValue(CSVRecord record, String columnName) {
        if (!record.isMapped(columnName)) {
            return "";
        }

        String value = record.get(columnName);

        if (value == null) {
            return "";
        }

        return value.trim();
    }

    private String getFirstAvailableValue(CSVRecord record, String... possibleColumnNames) {
        for (String columnName : possibleColumnNames) {
            if (record.isMapped(columnName)) {
                return getValue(record, columnName);
            }
        }

        return "";
    }

    private int parseInt(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public TimetablingDataset loadTimetablingDataset() throws Exception {
    return new TimetablingDataset(
            loadRooms(),
            loadScheduleEntries()
    );
    }

    private String normalizeRoomName(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return value.trim()
                .replace(" ", "_")
                .replaceAll("_+", "_");
    }

    private Set<String> extractRoomFeatures(CSVRecord record) {
        Set<String> features = new HashSet<>();

        Set<String> ignoredColumns = Set.of(
                "Edifício",
                "Nome_sala",
                "Capacidade_Normal",
                "Capacidade_Exame",
                "Nº_características"
        );

        for (Map.Entry<String, String> entry : record.toMap().entrySet()) {
            String columnName = entry.getKey();
            String value = entry.getValue();

            if (ignoredColumns.contains(columnName)) {
                continue;
            }

            if (value != null && !value.isBlank()) {
                features.add(normalizeFeatureName(columnName));
            }
        }

        return features;
    }

    private String normalizeFeatureName(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return value.trim()
                .replace("_", " ")
                .replaceAll("\\s+", " ")
                .toLowerCase();
    }

    private Set<String> extractFeatureList(String value) {
        Set<String> features = new HashSet<>();

        if (value == null || value.isBlank()) {
            return features;
        }

        String[] parts = value.split(",");

        for (String part : parts) {
            String normalizedFeature = normalizeFeatureName(part);

            if (!normalizedFeature.isBlank()) {
                features.add(normalizedFeature);
            }
        }

        return features;
    }
}
