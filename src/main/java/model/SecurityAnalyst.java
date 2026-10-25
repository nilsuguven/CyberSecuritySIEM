package model;

public class SecurityAnalyst {
    private String analystId;
    private String name;
    private String role;

    public SecurityAnalyst(String analystId, String name, String role) {
        this.analystId = analystId;
        this.name = name;
        this.role = role;
    }

    public String getAnalystId() { return analystId; }
    public String getName() { return name; }
    public String getRole() { return role; }
}