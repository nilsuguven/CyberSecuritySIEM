package observer;

import model.Incident;
import java.util.ArrayList;
import java.util.List;

public class AlertPublisher {
    private final List<AlertObserver> observers = new ArrayList<>();

    public void subscribe(AlertObserver observer) {
        observers.add(observer);
    }

    public void unsubscribe(AlertObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(Incident incident) {
        for (AlertObserver observer : observers) {
            observer.onCriticalIncident(incident);
        }
    }
}