package observer;

import model.Incident;

public interface AlertObserver {
    void onCriticalIncident(Incident incident);
}