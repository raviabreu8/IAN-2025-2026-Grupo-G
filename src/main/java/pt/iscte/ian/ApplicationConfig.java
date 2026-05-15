package pt.iscte.ian;

public class ApplicationConfig {

    private final String ollamaApiUrl;
    private final String ollamaModel;
    private final int maxCorrectionAttempts;

    public ApplicationConfig(String ollamaApiUrl, String ollamaModel, int maxCorrectionAttempts) {
        this.ollamaApiUrl = ollamaApiUrl;
        this.ollamaModel = ollamaModel;
        this.maxCorrectionAttempts = maxCorrectionAttempts;
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

    @Override
    public String toString() {
        return "ApplicationConfig{" +
                "ollamaApiUrl='" + ollamaApiUrl + '\'' +
                ", ollamaModel='" + ollamaModel + '\'' +
                ", maxCorrectionAttempts=" + maxCorrectionAttempts +
                '}';
    }
}