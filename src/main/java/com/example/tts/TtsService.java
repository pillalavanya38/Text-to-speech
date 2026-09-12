
package com.example.tts;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class TtsService {

    private final Path audioDirectory = Paths.get("generated-audio");

    private final String apiKey = System.getenv("ELEVENLABS_API_KEY");

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String generateAudio(
            String text,
            String language,
            String voice
    ) throws IOException, InterruptedException {

        Files.createDirectories(audioDirectory);

        if (apiKey == null || apiKey.isBlank()) {
            throw new IOException(
                    "ELEVENLABS_API_KEY environment variable is not set."
            );
        }

        String voiceId = getVoiceId(language, voice);

        String fileName =
                "speech-" + System.currentTimeMillis() + ".mp3";

        Path audioFile =
                audioDirectory.resolve(fileName);

        String jsonBody = "{"
                + "\"text\":\"" + escapeJson(text) + "\","
                + "\"model_id\":\"eleven_multilingual_v2\""
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "https://api.elevenlabs.io/v1/text-to-speech/"
                                + voiceId
                ))
                .header("xi-api-key", apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "audio/mpeg")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<byte[]> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofByteArray()
                );

        if (response.statusCode() < 200 ||
                response.statusCode() >= 300) {

            String errorMessage =
                    new String(
                            response.body(),
                            java.nio.charset.StandardCharsets.UTF_8
                    );

            throw new IOException(
                    "ElevenLabs API error "
                            + response.statusCode()
                            + ": "
                            + errorMessage
            );
        }

        Files.write(audioFile, response.body());

        if (!Files.exists(audioFile) ||
                Files.size(audioFile) == 0) {

            Files.deleteIfExists(audioFile);

            throw new IOException(
                    "ElevenLabs generated an empty audio file."
            );
        }

        return fileName;
    }

    private String getVoiceId(
            String language,
            String voice
    ) {

        String selectedLanguage =
                language == null || language.trim().isEmpty()
                        ? "en-US"
                        : language.trim();

        String selectedVoice =
                voice == null || voice.trim().isEmpty()
                        ? "female"
                        : voice.trim().toLowerCase();

        /*
         * These are ElevenLabs voice IDs.
         * We will replace them with the exact voices
         * available in your ElevenLabs account if needed.
         */

        switch (selectedLanguage) {

            case "en-US":
            case "en-IN":
                return selectedVoice.equals("male")
                        ? "pNInz6obpgDQGcFmaJgB"
                        : "EXAVITQu4vr4xnSDxMaL";

            case "hi-IN":
                return selectedVoice.equals("male")
                        ? "pNInz6obpgDQGcFmaJgB"
                        : "EXAVITQu4vr4xnSDxMaL";

            case "gu-IN":
                return selectedVoice.equals("male")
                        ? "pNInz6obpgDQGcFmaJgB"
                        : "EXAVITQu4vr4xnSDxMaL";

            case "mr-IN":
                return selectedVoice.equals("male")
                        ? "pNInz6obpgDQGcFmaJgB"
                        : "EXAVITQu4vr4xnSDxMaL";

            case "es-ES":
                return selectedVoice.equals("male")
                        ? "pNInz6obpgDQGcFmaJgB"
                        : "EXAVITQu4vr4xnSDxMaL";

            case "fr-FR":
                return selectedVoice.equals("male")
                        ? "pNInz6obpgDQGcFmaJgB"
                        : "EXAVITQu4vr4xnSDxMaL";

            case "de-DE":
                return selectedVoice.equals("male")
                        ? "pNInz6obpgDQGcFmaJgB"
                        : "EXAVITQu4vr4xnSDxMaL";

            default:
                throw new IllegalArgumentException(
                        "Unsupported language: "
                                + selectedLanguage
                );
        }
    }

    private String escapeJson(String text) {

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}

