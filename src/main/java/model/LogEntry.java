package model;

import java.time.LocalDateTime;

public class LogEntry {
    private String id;
    private String sourceIp;
    private String message;
    private String logType;
    private LocalDateTime timestamp;

    public LogEntry(String id, String sourceIp, String message, String logType) {
        this.id = id;
        this.sourceIp = sourceIp;
        this.message = message;
        this.logType = logType;
        this.timestamp = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getSourceIp() { return sourceIp; }
    public String getMessage() { return message; }
    public String getLogType() { return logType; }
    public LocalDateTime getTimestamp() { return timestamp; }
}