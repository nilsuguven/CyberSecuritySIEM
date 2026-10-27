package model;

import java.time.LocalDateTime;

public class Incident {
    private String id;
    private String title;
    private String sourceIp;
    private SeverityLevel severity;
    private int threatScore;
    private String status;
    private LocalDateTime timestamp;

    // 1. Ana Constructor (7 Parametreli - Kalıcılık ve Zaman Damgası Koruma İçin)
    public Incident(String id, String title, String sourceIp, SeverityLevel severity, int threatScore, String status, LocalDateTime timestamp) {
        this.id = id;
        this.title = title;
        this.sourceIp = sourceIp;
        this.severity = severity;
        this.threatScore = threatScore;
        this.status = status;
        this.timestamp = (timestamp != null) ? timestamp : LocalDateTime.now();
    }

    // 2. Kolaylık Constructor'ı (5 Parametreli - Yeni Olay Oluşturma İçin)
    public Incident(String id, String title, String sourceIp, SeverityLevel severity, int threatScore) {
        this(id, title, sourceIp, severity, threatScore, "OPEN", LocalDateTime.now());
    }

    // Getter ve Setter Metotları
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getSourceIp() { return sourceIp; }
    public SeverityLevel getSeverity() { return severity; }
    public int getThreatScore() { return threatScore; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return id + "," + title + "," + sourceIp + "," + severity + "," + threatScore + "," + status + "," + timestamp;
    }
}