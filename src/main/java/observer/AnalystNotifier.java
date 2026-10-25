package observer;

import model.Incident;

public class AnalystNotifier implements AlertObserver {
    private String channelName;

    public AnalystNotifier(String channelName) {
        this.channelName = channelName;
    }

    @Override
    public void onCriticalIncident(Incident incident) {
        System.out.println("🚨 [" + channelName + " UYARISI] KRİTİK GÜVENLİK OLAYI! -> " + incident.getTitle() + " (IP: " + incident.getSourceIp() + ")");
    }
}