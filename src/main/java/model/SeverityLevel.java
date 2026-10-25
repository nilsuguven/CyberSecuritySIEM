package model;

public enum SeverityLevel {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int level;

    SeverityLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}