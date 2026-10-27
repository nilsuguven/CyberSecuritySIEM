package repository;
import model.Incident;
import model.SeverityLevel;
import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileRepository implements IRepository<Incident> {
    private final String filePath = "incidents.txt";

    @Override
    public void saveAll(List<Incident> incidents) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Incident inc : incidents) {
                writer.println(inc.getId() + "|" +
                        inc.getTitle() + "|" +
                        inc.getSourceIp() + "|" +
                        inc.getSeverity() + "|" +
                        inc.getThreatScore() + "|" +
                        inc.getStatus() + "|" +
                        inc.getTimestamp());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Incident> loadAll() {
        List<Incident> list = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) return list;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 7) {
                    String id = parts[0];
                    String title = parts[1];
                    String ip = parts[2];
                    SeverityLevel severity = SeverityLevel.valueOf(parts[3]);
                    int score = Integer.parseInt(parts[4]);
                    String status = parts[5];
                    LocalDateTime timestamp = LocalDateTime.parse(parts[6]);

                    Incident incident = new Incident(id, title, ip, severity, score, status, timestamp);
                    list.add(incident);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}