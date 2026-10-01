
package com.example.tts;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;

@Service
public class TtsService {

    private static final String WINDOWS_PYTHON_PATH =
            "C:\\Users\\DELL\\AppData\\Local\\Python\\bin\\python.exe";

    public String generateAudio(
            String text,
            String language,
            String voice) throws Exception {

        if (text == null || text.trim().isEmpty()) {
            throw new RuntimeException("Text cannot be empty.");
        }

        if (language == null || language.trim().isEmpty()) {
            throw new RuntimeException("Language cannot be empty.");
        }

        return generateWithPiper(text, language, voice);
    }

    private String generateWithPiper(
            String text,
            String language,
            String voice) throws Exception {

        String model = getPiperModel(language, voice);

        Path outputDirectory = Paths.get("generated-audio");

        Files.createDirectories(outputDirectory);

        String timestamp =
                String.valueOf(System.currentTimeMillis());

        String fileName =
                "piper-" + timestamp + ".wav";

        Path audioFile =
                outputDirectory.resolve(fileName);

        Path inputFile =
                outputDirectory.resolve(
                        "piper-input-" + timestamp + ".txt"
                );

        String safeText = removeInvalidUnicode(text);

        Files.writeString(
                inputFile,
                safeText,
                StandardCharsets.UTF_8
        );

        String pythonCommand = getPythonCommand();

        System.out.println("--------------------------------");
        System.out.println("Starting Piper TTS");
        System.out.println("Operating System: "
                + System.getProperty("os.name"));
        System.out.println("Python Command: "
                + pythonCommand);
        System.out.println("Language: " + language);
        System.out.println("Voice: " + voice);
        System.out.println("Model: " + model);
        System.out.println("Input: " + safeText);
        System.out.println(
                "Output: "
                        + audioFile.toAbsolutePath()
        );
        System.out.println("--------------------------------");

        ProcessBuilder processBuilder =
                new ProcessBuilder(
                        pythonCommand,
                        "-m",
                        "piper",
                        "-m",
                        model,
                        "--input_file",
                        inputFile.toAbsolutePath().toString(),
                        "--output_file",
                        audioFile.toAbsolutePath().toString()
                );

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        String processOutput =
                new String(
                        process.getInputStream().readAllBytes(),
                        StandardCharsets.UTF_8
                );

        int exitCode = process.waitFor();

        System.out.println(
                "Piper exit code: " + exitCode
        );

        System.out.println("Piper output:");
        System.out.println(processOutput);

        try {
            Files.deleteIfExists(inputFile);
        } catch (Exception ignored) {
        }

        if (exitCode != 0) {
            throw new RuntimeException(
                    "Piper TTS failed: " + processOutput
            );
        }

        if (!Files.exists(audioFile)) {
            throw new RuntimeException(
                    "Piper did not create the audio file."
            );
        }

        if (Files.size(audioFile) == 0) {
            throw new RuntimeException(
                    "Piper created an empty audio file."
            );
        }

        System.out.println(
                "Piper audio saved successfully: "
                        + audioFile.toAbsolutePath()
        );

        return fileName;
    }

    private String getPythonCommand() {

        String operatingSystem =
                System.getProperty("os.name")
                        .toLowerCase();

        // Windows
        if (operatingSystem.contains("win")) {

            Path windowsPython =
                    Paths.get(WINDOWS_PYTHON_PATH);

            if (!Files.exists(windowsPython)) {
                throw new RuntimeException(
                        "Python was not found at: "
                                + WINDOWS_PYTHON_PATH
                );
            }

            return WINDOWS_PYTHON_PATH;
        }

        // Linux / Render
        return "python3";
    }

    private String getPiperModel(
            String language,
            String voice) {

        boolean female =
                voice != null
                        && voice.equalsIgnoreCase("female");

        switch (language.toLowerCase()) {

            // English
            case "en-in":
            case "en-us":
            case "en-gb":

                if (female) {
                    return "en_US-lessac-medium";
                }

                return "en_US-ryan-medium";

            // Hindi
            case "hi-in":

                if (female) {
                    return "hi_IN-priyamvada-medium";
                }

                return "hi_IN-pratham-medium";

            // Gujarati
            case "gu-in":

                throw new RuntimeException(
                        "Gujarati Piper voice needs to be configured."
                );

            // Marathi
            case "mr-in":

                return "mr_IN-google-medium";

            // Spanish
            case "es-es":

                return "es_ES-davefx-medium";

            // German
            case "de-de":

                if (female) {
                    return "de_DE-kerstin-low";
                }

                return "de_DE-thorsten-medium";

            // French
            case "fr-fr":

                return "fr_FR-siwis-medium";

            default:

                throw new RuntimeException(
                        "Piper model is not configured for language: "
                                + language
                );
        }
    }

    private String removeInvalidUnicode(String text) {

        if (text == null) {
            return "";
        }

        StringBuilder result =
                new StringBuilder();

        for (int i = 0; i < text.length(); i++) {

            char current = text.charAt(i);

            if (!Character.isSurrogate(current)) {

                result.append(current);

                continue;
            }

            if (
                    Character.isHighSurrogate(current)
                            && i + 1 < text.length()
                            && Character.isLowSurrogate(
                                    text.charAt(i + 1)
                            )
            ) {

                result.append(current);

                result.append(
                        text.charAt(i + 1)
                );

                i++;
            }
        }

        return result.toString();
    }
}

