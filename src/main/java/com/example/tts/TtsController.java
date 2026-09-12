
package com.example.tts;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tts")
@CrossOrigin(origins = "http://localhost:5173")
public class TtsController {

    private final TtsService ttsService;

    public TtsController(TtsService ttsService) {
        this.ttsService = ttsService;
    }

    // Generate audio
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateSpeech(
            @RequestBody TtsRequest request) {

        Map<String, Object> response = new HashMap<>();

        if (request.getText() == null ||
                request.getText().trim().isEmpty()) {

            response.put("success", false);
            response.put("message", "Please enter some text.");

            return ResponseEntity.badRequest().body(response);
        }

        if (request.getText().length() > 1000) {

            response.put("success", false);
            response.put(
                    "message",
                    "Text cannot exceed 1000 characters."
            );

            return ResponseEntity.badRequest().body(response);
        }

        try {

            String fileName = ttsService.generateAudio(
                    request.getText(),
                    request.getLanguage(),
                    request.getVoice()
            );

            response.put("success", true);

            response.put(
                    "message",
                    "Speech generated successfully."
            );

            response.put("fileName", fileName);

            response.put(
                    "audioUrl",
                    "/api/tts/audio/" + fileName
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            e.printStackTrace();

            response.put("success", false);

            response.put(
                    "message",
                    "Unable to generate audio: " +
                            e.getMessage()
            );

            return ResponseEntity
                    .internalServerError()
                    .body(response);
        }
    }

    // Play / access generated MP3 file
    @GetMapping("/audio/{fileName}")
    public ResponseEntity<Resource> getAudio(
            @PathVariable String fileName) {

        if (fileName.contains("..") ||
                fileName.contains("/") ||
                fileName.contains("\\")) {

            return ResponseEntity.badRequest().build();
        }

        Path audioPath =
                Paths.get("generated-audio")
                        .resolve(fileName)
                        .normalize();

        Resource resource =
                new FileSystemResource(audioPath);

        if (!resource.exists() ||
                !resource.isReadable()) {

            return ResponseEntity.notFound().build();
        }

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" +
                                fileName +
                                "\""
                )
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(resource);
    }

    // Download generated MP3 file
    @GetMapping("/audio/{fileName}/download")
    public ResponseEntity<Resource> downloadAudio(
            @PathVariable String fileName) {

        if (fileName.contains("..") ||
                fileName.contains("/") ||
                fileName.contains("\\")) {

            return ResponseEntity.badRequest().build();
        }

        Path audioPath =
                Paths.get("generated-audio")
                        .resolve(fileName)
                        .normalize();

        Resource resource =
                new FileSystemResource(audioPath);

        if (!resource.exists() ||
                !resource.isReadable()) {

            return ResponseEntity.notFound().build();
        }

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                fileName +
                                "\""
                )
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(resource);
    }
}

