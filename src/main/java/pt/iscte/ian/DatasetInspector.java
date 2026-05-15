package pt.iscte.ian;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DatasetInspector {

    public void inspectDatasets() throws Exception {
        inspectCsv("Caracterização das salas", Path.of("data", "input", "Caracterizacao das salas.csv"));
        inspectCsv("Horários 1º semestre 2022-2023", Path.of("data", "input", "Horários 1º sem 2022-23.csv"));
    }

    private void inspectCsv(String datasetName, Path filePath) throws Exception {
        System.out.println();
        System.out.println("A analisar dataset: " + datasetName);
        System.out.println("Ficheiro: " + filePath.toAbsolutePath());

        if (!Files.exists(filePath)) {
            System.out.println("ERRO: ficheiro não encontrado.");
            return;
        }

        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT
                     .builder()
                     .setDelimiter(';')
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {

            List<String> headers = parser.getHeaderNames();

            System.out.println("Colunas encontradas:");
            for (String header : headers) {
                System.out.println("- " + header);
            }

            int numberOfRows = 0;

            System.out.println("Primeiras linhas:");

            for (CSVRecord record : parser) {
                numberOfRows++;

                if (numberOfRows <= 3) {
                    System.out.println(record.toMap());
                }
            }

            System.out.println("Número total de linhas: " + numberOfRows);
        }
    }
}