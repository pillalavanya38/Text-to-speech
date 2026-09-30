package com.example.tts;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;

@Service
public class TtsService {

    public String generateAudio(
            String text,
            String language,
            String voice) throws Exception {

        return generateWithPiper(text, language, voice);
    }

    private String generateWithPiper(
            String text,
            String language,
            String voice) throws Exception {

        String model = getPiperModel(language, voice);

        Path outputDirectory =
                Paths.get("generated-audio");

        Files.createDirectories(outputDirectory);

        String fileName =
                "piper-" + System.currentTimeMillis() + ".wav";

        Path audioFile =
                outputDirectory.resolve(fileName);

        /*
         * Create a UTF-8 input file.
         * This avoids Windows stdin encoding problems.
         */
        Path inputFile =
                outputDirectory.resolve(
                        "piper-input-" + System.currentTimeMillis() + ".txt"
                );

        String safeText = removeInvalidUnicode(text);

        Files.writeString(
                inputFile,
                safeText,
                StandardCharsets.UTF_8
        );

        ProcessBuilder processBuilder = new ProcessBuilder(
                "python",
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

        System.out.println("Piper model: " + model);
        System.out.println("Piper input file: "
                + inputFile.toAbsolutePath());
        System.out.println("Piper output:");
        System.out.println(processOutput);

        /*
         * Delete temporary input file
         */
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

        System.out.println(
                "Audio saved: "
                        + audioFile.toAbsolutePath()
        );

        return fileName;
    }

    private String removeInvalidUnicode(String text) {

        if (text == null) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {

            char current = text.charAt(i);

            /*
             * Normal character
             */
            if (!Character.isSurrogate(current)) {
                result.append(current);
                continue;
            }

            /*
             * Valid surrogate pair
             */
            if (Character.isHighSurrogate(current)
                    && i + 1 < text.length()
                    && Character.isLowSurrogate(
                            text.charAt(i + 1))) {

                result.append(current);
                result.append(text.charAt(i + 1));

                i++;
            }

            /*
             * Invalid surrogate is ignored.
             */
        }

        return result.toString();
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
                return female
                        ? "en_US-hfc_female-medium"
                        : "en_US-hfc_male-medium";

            // Hindi
            case "hi-in":
                return female
                        ? "hi_IN-priyamvada-medium"
                        : "hi_IN-pratham-medium";

            // Gujarati
            case "gu-in":
                return "gu_IN-dhwani-medium";

            // Marathi
            case "mr-in":
                return "mr_IN-google-medium";

            // Telugu
            case "te-in":
                return female
                        ? "te_IN-padmavathi-medium"
                        : "te_IN-venkatesh-medium";

            // Spanish
            case "es-es":
                return "es_ES-davefx-medium";

            // French
            case "fr-fr":
                return "fr_FR-siwis-medium";

            // German
            case "de-de":
                return "de_DE-kerstin-low";

            default:
                throw new RuntimeException(
                        "No Piper model configured for language: "
                                + language
                );
        }
    }
}