package observer;

import model.Incident;

public class AnalystNotifier implements AlertObserver {
    private final String analystName;
    private Incident lastNotifiedIncident;
    private int notificationCount = 0;

    public AnalystNotifier(String analystName) {
        this.analystName = analystName;
    }

    @Override
    public void update(Incident incident) {
        this.lastNotifiedIncident = incident;
        this.notificationCount++;
        System.out.println(analystName + " notified of incident: " + incident.getId());
    }

    public Incident getLastNotifiedIncident() {
        return lastNotifiedIncident;
    }

    public int getNotificationCount() {
        return notificationCount;
    }
}