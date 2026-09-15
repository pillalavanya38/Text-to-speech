package com.example.tts;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class TtsService {

    private final Path audioDirectory = Paths.get("generated-audio");

    public String generateAudio(
            String text,
            String language,
            String voice
    ) throws IOException {

        Files.createDirectories(audioDirectory);

        if (text == null || text.trim().isEmpty()) {
            throw new IOException("Text cannot be empty");
        }

        if (text.length() > 1000) {
            throw new IOException("Text cannot exceed 1000 characters");
        }

        String fileName = "speech-" + System.currentTimeMillis() + ".wav";
        Path audioFile = audioDirectory.resolve(fileName);

        try {
            if (language.equalsIgnoreCase("en-IN")
                    || language.equalsIgnoreCase("hi-IN")
                    || language.equalsIgnoreCase("gu-IN")
                    || language.equalsIgnoreCase("mr-IN")) {

                generateWithSarvam(text, language, voice, audioFile);

            } else if (language.equalsIgnoreCase("es-ES")
                    || language.equalsIgnoreCase("fr-FR")
                    || language.equalsIgnoreCase("de-DE")) {

                generateWithPiper(text, language, voice, audioFile);

            } else {
                throw new IOException(
                        "Unsupported language: " + language
                );
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("TTS process was interrupted", e);
        }

        if (!Files.exists(audioFile) || Files.size(audioFile) == 0) {
            throw new IOException("Audio file was not generated");
        }

        return fileName;
    }

    // ============================================================
    // SARVAM TTS
    // English, Hindi, Gujarati, Marathi
    // ============================================================

    private void generateWithSarvam(
            String text,
            String language,
            String voice,
            Path audioFile
    ) throws IOException, InterruptedException {

        String apiKey = System.getenv("SARVAM_API_KEY");

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IOException(
                    "SARVAM_API_KEY environment variable is not set"
            );
        }

        String speaker;

        if (voice != null && voice.equalsIgnoreCase("male")) {
            speaker = "ratan";
        } else {
            speaker = "priya";
        }

        JSONObject requestJson = new JSONObject();

        requestJson.put("text", text);
        requestJson.put("target_language_code", language);
        requestJson.put("speaker", speaker);
        requestJson.put("model", "bulbul:v3");
        requestJson.put("speech_sample_rate", 22050);
        requestJson.put("output_audio_codec", "wav");

        System.out.println(
                "Sarvam language: " + language
                        + " | Voice: " + voice
                        + " | Speaker: " + speaker
        );

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "https://api.sarvam.ai/text-to-speech"
                ))
                .header(
                        "api-subscription-key",
                        apiKey
                )
                .header(
                        "Content-Type",
                        "application/json"
                )
                .POST(
                        HttpRequest.BodyPublishers.ofString(
                                requestJson.toString()
                        )
                )
                .build();

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        System.out.println(
                "Sarvam response status: "
                        + response.statusCode()
        );

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            throw new IOException(
                    "Sarvam TTS failed: "
                            + response.body()
            );
        }

        JSONObject responseJson =
                new JSONObject(response.body());

        if (!responseJson.has("audios")) {
            throw new IOException(
                    "Sarvam response does not contain audio"
            );
        }

        String base64Audio =
                responseJson
                        .getJSONArray("audios")
                        .getString(0);

        byte[] audioBytes =
                Base64.getDecoder().decode(base64Audio);

        Files.write(audioFile, audioBytes);
    }

    // ============================================================
    // PIPER TTS
    // Spanish, French, German
    // ============================================================

    private void generateWithPiper(
            String text,
            String language,
            String voice,
            Path audioFile
    ) throws IOException, InterruptedException {

        boolean isMale =
                voice != null
                        && voice.equalsIgnoreCase("male");

        String model;

        // --------------------------------------------------------
        // SPANISH
        // --------------------------------------------------------
        if (language.equalsIgnoreCase("es-ES")) {

            if (isMale) {
                model = "es_ES-davefx-medium";
            } else {
                model = "es_AR-daniela-high";
            }

        // --------------------------------------------------------
        // FRENCH
        // --------------------------------------------------------
        } else if (language.equalsIgnoreCase("fr-FR")) {

            if (isMale) {
                model = "fr_FR-siwis-medium";
            } else {
                model = "fr_FR-gilles-low";
            }

        // --------------------------------------------------------
        // GERMAN
        // --------------------------------------------------------
        } else if (language.equalsIgnoreCase("de-DE")) {

            if (isMale) {
                model = "de_DE-thorsten-medium";
            } else {
                model = "de_DE-kerstin-low";
            }

        } else {
            throw new IOException(
                    "Piper does not support language: "
                            + language
            );
        }

        System.out.println(
                "========================================"
        );

        System.out.println(
                "Piper language: " + language
        );

        System.out.println(
                "Selected voice: " + voice
        );

        System.out.println(
                "Selected Piper model: " + model
        );

        System.out.println(
                "========================================"
        );

        ProcessBuilder processBuilder =
                new ProcessBuilder(
                        "python",
                        "-m",
                        "piper",
                        "-m",
                        model,
                        "--output_file",
                        audioFile.toAbsolutePath().toString()
                );

        processBuilder.redirectErrorStream(true);

        Process process =
                processBuilder.start();

        // Send text to Piper
        process.getOutputStream().write(
                text.getBytes(StandardCharsets.UTF_8)
        );

        process.getOutputStream().close();

        // Read Piper output
        String output =
                new String(
                        process.getInputStream().readAllBytes(),
                        StandardCharsets.UTF_8
                );

        int exitCode = process.waitFor();

        System.out.println(
                "Piper exit code: " + exitCode
        );

        if (!output.trim().isEmpty()) {
            System.out.println(
                    "Piper output: " + output
            );
        }

        if (exitCode != 0) {
            throw new IOException(
                    "Piper TTS failed: " + output
            );
        }

        if (!Files.exists(audioFile)
                || Files.size(audioFile) == 0) {

            throw new IOException(
                    "Piper did not generate audio file"
            );
        }

        System.out.println(
                "Piper audio generated successfully: "
                        + audioFile
        );
    }
}