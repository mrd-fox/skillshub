package com.simplon_project.skillhub.skillhub.storage.application.worker;

import com.simplon_project.skillhub.skillhub.storage.domaine.model.VideoMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;

@Component
@Slf4j
@ConditionalOnProperty(prefix = "storage", name = "enabled", havingValue = "true")
public class VideoMetadataExtractor {

    public VideoMetadata extract(Path file) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe",
                    "-v", "error",
                    "-select_streams", "v:0",
                    "-show_entries", "stream=width,height,duration",
                    "-of", "csv=p=0",
                    file.toAbsolutePath().toString()
            );

            Process process = pb.start();
            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String output = reader.readLine(); // ex: "1920,1080,123.456"
                int exitCode = process.waitFor();

                if (exitCode != 0 || output == null || output.isBlank()) {
                    throw new IllegalStateException("ffprobe returned no data for file: " + file);
                }

                String[] parts = output.split(",");
                int width = (int) Double.parseDouble(parts[0]);
                int height = (int) Double.parseDouble(parts[1]);
                int duration = (int) Math.round(Double.parseDouble(parts[2]));

                log.info("🔍 [ffprobe] {} -> {}x{}, {}s", file.getFileName(), width, height, duration);
                return new VideoMetadata(width, height, duration);
            }

        } catch (Exception e) {
            log.error("Erreur lors de l'analyse vidéo avec ffprobe: {}", e.getMessage());
            throw new RuntimeException("Failed to extract video metadata", e);
        }
    }
}