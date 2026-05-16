package pt.iscte.ian;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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

                rooms.add(new Room(
                        building,
                        name,
                        normalCapacity,
                        examCapacity
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
                String requestedRoomFeatures = getValue(record, "Características da sala pedida para a aula");
                String roomName = normalizeRoomName(getValue(record, "Sala da aula"));
                int roomCapacity = parseInt(getValue(record, "Lotação"));
                String realRoomFeatures = getValue(record, "Características reais da sala");

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
                        requestedRoomFeatures,
                        roomName,
                        roomCapacity,
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
}
