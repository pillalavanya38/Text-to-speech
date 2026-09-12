package com.example.tts;

import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.pemistahl.lingua.api.Language.ENGLISH;
import static com.github.pemistahl.lingua.api.Language.FRENCH;
import static com.github.pemistahl.lingua.api.Language.GERMAN;
import static com.github.pemistahl.lingua.api.Language.GUJARATI;
import static com.github.pemistahl.lingua.api.Language.HINDI;
import static com.github.pemistahl.lingua.api.Language.MARATHI;
import static com.github.pemistahl.lingua.api.Language.SPANISH;

@Service
public class TtsService {

    private final Path audioDirectory =
            Paths.get("generated-audio");

    private final String apiKey =
            System.getenv("ELEVENLABS_API_KEY");

    private final HttpClient httpClient =
            HttpClient.newHttpClient();

    /*
     * Language detector.
     *
     * It checks only the languages supported
     * by our TTS application.
     */
    private final LanguageDetector languageDetector =
            LanguageDetectorBuilder
                    .fromLanguages(
                            ENGLISH,
                            HINDI,
                            GUJARATI,
                            MARATHI,
                            SPANISH,
                            FRENCH,
                            GERMAN
                    )
                    .build();

    public String generateAudio(
            String text,
            String language,
            String voice
    ) throws IOException, InterruptedException {

        Files.createDirectories(audioDirectory);

        // Check ElevenLabs API key
        if (apiKey == null || apiKey.isBlank()) {
            throw new IOException(
                    "ELEVENLABS_API_KEY environment variable is not set on the server."
            );
        }

        // Validate selected language and entered text
        validateLanguage(text, language);

        // Get ElevenLabs voice ID
        String voiceId =
                getVoiceId(language, voice);

        // Create unique audio file name
        String fileName =
                "speech-" + System.currentTimeMillis() + ".mp3";

        Path audioFile =
                audioDirectory.resolve(fileName);

        // ElevenLabs request body
        String jsonBody =
                "{"
                        + "\"text\":\""
                        + escapeJson(text)
                        + "\","
                        + "\"model_id\":\"eleven_multilingual_v2\""
                        + "}";

        // ElevenLabs API request
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        "https://api.elevenlabs.io/v1/text-to-speech/"
                                                + voiceId
                                )
                        )
                        .header(
                                "xi-api-key",
                                apiKey
                        )
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .header(
                                "Accept",
                                "audio/mpeg"
                        )
                        .POST(
                                HttpRequest.BodyPublishers.ofString(
                                        jsonBody
                                )
                        )
                        .build();

        // Send request to ElevenLabs
        HttpResponse<byte[]> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofByteArray()
                );

        // Handle ElevenLabs errors
        if (response.statusCode() < 200 ||
                response.statusCode() >= 300) {

            String errorMessage =
                    new String(
                            response.body(),
                            StandardCharsets.UTF_8
                    );

            throw new IOException(
                    "ElevenLabs API error "
                            + response.statusCode()
                            + ": "
                            + errorMessage
            );
        }

        // Save generated audio
        Files.write(
                audioFile,
                response.body()
        );

        // Check generated file
        if (!Files.exists(audioFile) ||
                Files.size(audioFile) == 0) {

            Files.deleteIfExists(audioFile);

            throw new IOException(
                    "ElevenLabs generated an empty audio file."
            );
        }

        return fileName;
    }

    /**
     * Detects the actual language of the entered text
     * and compares it with the language selected by
     * the user.
     */
    private void validateLanguage(
            String text,
            String language
    ) {

        if (language == null ||
                language.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Please select a language."
            );
        }

        if (text == null ||
                text.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Please enter some text."
            );
        }

        String selectedLanguage =
                language.trim();

        /*
         * Detect actual language.
         */
        Language detectedLanguage =
                languageDetector.detectLanguageOf(
                        text.trim()
                );

        /*
         * Convert selected language code
         * to Lingua language.
         */
        Language expectedLanguage =
                getExpectedLanguage(
                        selectedLanguage
                );

        /*
         * Compare selected language
         * with detected language.
         */
        if (detectedLanguage != expectedLanguage) {

            throw new IllegalArgumentException(
                    "Language mismatch. You selected "
                            + getLanguageName(expectedLanguage)
                            + " but the entered text appears to be "
                            + getLanguageName(detectedLanguage)
                            + ". Please enter text in "
                            + getLanguageName(expectedLanguage)
                            + "."
            );
        }
    }

    /**
     * Converts frontend language code
     * into Lingua language.
     */
    private Language getExpectedLanguage(
            String language
    ) {

        switch (language) {

            case "en-US":
            case "en-IN":
                return ENGLISH;

            case "hi-IN":
                return HINDI;

            case "gu-IN":
                return GUJARATI;

            case "mr-IN":
                return MARATHI;

            case "es-ES":
                return SPANISH;

            case "fr-FR":
                return FRENCH;

            case "de-DE":
                return GERMAN;

            default:
                throw new IllegalArgumentException(
                        "Unsupported language: "
                                + language
                );
        }
    }

    /**
     * Returns a user-friendly language name.
     */
    private String getLanguageName(
            Language language
    ) {

        switch (language) {

            case ENGLISH:
                return "English";

            case HINDI:
                return "Hindi";

            case GUJARATI:
                return "Gujarati";

            case MARATHI:
                return "Marathi";

            case SPANISH:
                return "Spanish";

            case FRENCH:
                return "French";

            case GERMAN:
                return "German";

            default:
                return language.name();
        }
    }

    /**
     * Returns the ElevenLabs voice ID.
     */
    private String getVoiceId(
            String language,
            String voice
    ) {

        String selectedLanguage =
                language == null ||
                        language.trim().isEmpty()
                        ? "en-US"
                        : language.trim();

        String selectedVoice =
                voice == null ||
                        voice.trim().isEmpty()
                        ? "female"
                        : voice.trim().toLowerCase();

        switch (selectedLanguage) {

            case "en-US":
            case "en-IN":
            case "hi-IN":
            case "gu-IN":
            case "mr-IN":
            case "es-ES":
            case "fr-FR":
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

    /**
     * Escapes special characters before
     * putting text inside JSON.
     */
    private String escapeJson(
            String text
    ) {

        if (text == null) {
            return "";
        }

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}