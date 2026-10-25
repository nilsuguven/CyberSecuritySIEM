package model;

import java.time.LocalDateTime;

public class Incident implements Comparable<Incident> {
    private String id;
    private String title;
    private String sourceIp;
    private SeverityLevel severity;
    private int threatScore;
    private LocalDateTime timestamp;
    private String status;

    public Incident(String id, String title, String sourceIp, SeverityLevel severity, int threatScore) {
        this.id = id;
        this.title = title;
        this.sourceIp = sourceIp;
        this.severity = severity;
        this.threatScore = threatScore;
        this.timestamp = LocalDateTime.now();
        this.status = "OPEN";
    }

    @Override
    public int compareTo(Incident other) {
        return Integer.compare(other.threatScore, this.threatScore);
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getSourceIp() { return sourceIp; }
    public SeverityLevel getSeverity() { return severity; }
    public int getThreatScore() { return threatScore; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("[%s] ID: %s | %s | Tehdit Skoru: %d | Durum: %s | IP: %s",
                severity, id, title, threatScore, status, sourceIp);
    }
}