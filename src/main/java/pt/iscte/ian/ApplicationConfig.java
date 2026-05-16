package pt.iscte.ian;

public class ApplicationConfig {

    private final String ollamaApiUrl;
    private final String ollamaModel;
    private final int maxCorrectionAttempts;
    private final int maxEntriesForOptimization;

    public ApplicationConfig(
            String ollamaApiUrl,
            String ollamaModel,
            int maxCorrectionAttempts,
            int maxEntriesForOptimization
    ) {
        this.ollamaApiUrl = ollamaApiUrl;
        this.ollamaModel = ollamaModel;
        this.maxCorrectionAttempts = maxCorrectionAttempts;
        this.maxEntriesForOptimization = maxEntriesForOptimization;
    }

    public String getOllamaApiUrl() {
        return ollamaApiUrl;
    }

    public String getOllamaModel() {
        return ollamaModel;
    }

    public int getMaxCorrectionAttempts() {
        return maxCorrectionAttempts;
    }

    public int getMaxEntriesForOptimization() {
        return maxEntriesForOptimization;
    }

    @Override
    public String toString() {
        return "ApplicationConfig{" +
                "ollamaApiUrl='" + ollamaApiUrl + '\'' +
                ", ollamaModel='" + ollamaModel + '\'' +
                ", maxCorrectionAttempts=" + maxCorrectionAttempts +
                ", maxEntriesForOptimization=" + maxEntriesForOptimization +
                '}';
    }
}