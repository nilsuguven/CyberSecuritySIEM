package observer;

import model.Incident;

public interface AlertObserver {
    void update(Incident incident);
}