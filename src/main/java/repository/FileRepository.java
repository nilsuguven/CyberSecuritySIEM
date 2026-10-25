package repository;

import model.Incident;
import model.SeverityLevel;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileRepository {
    private final String filePath = "incidents.txt";

    public void saveAll(List<Incident> incidents) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Incident inc : incidents) {
                // Format: ID|Title|SourceIp|Severity|ThreatScore|Status|Timestamp
                writer.println(String.format("%s|%s|%s|%s|%d|%s|%s",
                        inc.getId(),
                        inc.getTitle(),
                        inc.getSourceIp(),
                        inc.getSeverity().name(),
                        inc.getThreatScore(),
                        inc.getStatus(),
                        inc.getTimestamp() != null ? inc.getTimestamp().toString() : LocalDateTime.now().toString()
                ));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Incident> loadAll() {
        List<Incident> list = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) return list;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 7) {
                    Incident inc = new Incident(
                            parts[0], // id
                            parts[1], // title
                            parts[2], // sourceIp
                            SeverityLevel.valueOf(parts[3]), // severity
                            Integer.parseInt(parts[4]) // threatScore
                    );
                    inc.setStatus(parts[5]); // Status (OPEN / RESOLVED)
                    list.add(inc);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}