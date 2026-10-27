package model;

import java.time.LocalDateTime;

public class LogEntry {
    private String id;
    private String sourceIp;
    private String message;
    private String logType;
    private LocalDateTime timestamp;

    // Ana Constructor (4 Parametreli)
    public LogEntry(String id, String sourceIp, String message, String logType) {
        this.id = id;
        this.sourceIp = sourceIp;
        this.message = message;
        this.logType = logType;
        this.timestamp = LocalDateTime.now();
    }

    // Aşırı Yüklenmiş Constructor (2 Parametreli - Testler ve Filtreler İçin)
    public LogEntry(String sourceIp, String message) {
        this("LOG-" + System.currentTimeMillis(), sourceIp, message, "GENERAL");
    }

    public String getId() { return id; }
    public String getSourceIp() { return sourceIp; }
    public String getMessage() { return message; }
    public String getLogType() { return logType; }
    public LocalDateTime getTimestamp() { return timestamp; }
}