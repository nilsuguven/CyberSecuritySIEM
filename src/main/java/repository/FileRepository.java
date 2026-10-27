
package repository;

import model.Incident;
import model.SeverityLevel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileRepository implements IRepository<Incident> {

    private final String filePath = "incidents.txt";

    @Override
    public void saveAll(List<Incident> incidents) {
        try (BufferedWriter writer = Files.newBufferedWriter(
                Path.of(filePath), StandardCharsets.UTF_8)) {

            for (Incident inc : incidents) {
                writer.write(
                        inc.getId() + "|" +
                                inc.getTitle() + "|" +
                                inc.getSourceIp() + "|" +
                                inc.getSeverity() + "|" +
                                inc.getThreatScore() + "|" +
                                inc.getStatus() + "|" +
                                inc.getTimestamp()
                );
                writer.newLine();
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Olaylar dosyaya kaydedilemedi: " + filePath, e);
        }
    }

    @Override
    public List<Incident> loadAll() {
        List<Incident> incidents = new ArrayList<>();
        Path path = Path.of(filePath);

        if (!Files.exists(path)) {
            return incidents;
        }

        try (BufferedReader reader =
                     Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                try {
                    String[] parts = line.split("\\|", -1);

                    if (parts.length != 7) {
                        throw new IllegalArgumentException(
                                "Kayıt 7 alan içermiyor.");
                    }

                    String id = parts[0];
                    String title = parts[1];
                    String ip = parts[2];
                    SeverityLevel severity =
                            SeverityLevel.valueOf(parts[3]);
                    int score = Integer.parseInt(parts[4]);
                    String status = parts[5];
                    LocalDateTime timestamp =
                            LocalDateTime.parse(parts[6]);

                    Incident incident = new Incident(
                            id, title, ip, severity, score, status, timestamp);

                    incidents.add(incident);

                } catch (RuntimeException e) {
                    System.err.println(
                            "[Repository] " + lineNumber +
                                    ". satır yüklenemedi: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Olay dosyası okunamadı: " + filePath, e);
        }

        return incidents;
    }
}
