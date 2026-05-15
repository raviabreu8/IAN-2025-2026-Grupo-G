package pt.iscte.ian;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ProblemDescriptionLoader {

    public String loadProblemDescription() throws Exception {
        InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream("problem_description.json");

        if (inputStream == null) {
            throw new RuntimeException("Ficheiro problem_description.json não encontrado em src/main/resources.");
        }

        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
}