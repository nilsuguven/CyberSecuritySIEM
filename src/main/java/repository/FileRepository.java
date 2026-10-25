package repository;

import model.Incident;
import model.SeverityLevel;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileRepository implements IRepository<Incident> {
    private final String filePath = "incidents.txt";

    @Override
    public void saveAll(List<Incident> items) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Incident incident : items) {
                // Verileri CSV formatında (virgülle ayrılmış) dosyaya yazıyoruz
                writer.write(String.format("%s,%s,%s,%s,%d,%s\n",
                        incident.getId(),
                        incident.getTitle(),
                        incident.getSourceIp(),
                        incident.getSeverity().name(),
                        incident.getThreatScore(),
                        incident.getStatus()));
            }
        } catch (IOException e) {
            System.err.println("Dosyaya kaydetme hatası: " + e.getMessage());
        }
    }

    @Override
    public List<Incident> loadAll() {
        List<Incident> list = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            return list; // Dosya henüz oluşmadıysa boş liste dön
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    Incident incident = new Incident(
                            parts[0], // ID
                            parts[1], // Title
                            parts[2], // Source IP
                            SeverityLevel.valueOf(parts[3]), // Severity
                            Integer.parseInt(parts[4]) // Threat Score
                    );
                    incident.setStatus(parts[5]); // Status
                    list.add(incident);
                }
            }
        } catch (IOException e) {
            System.err.println("Dosyadan okuma hatası: " + e.getMessage());
        }
        return list;
    }
}