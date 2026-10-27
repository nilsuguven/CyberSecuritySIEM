package service;

import filter.LogFilter;
import model.Incident;
import model.LogEntry;
import observer.AlertPublisher;
import repository.IRepository;

import java.util.List;

public class SIEMEngine {
    private final LogFilter filterChain;
    private final IRepository<Incident> repository; // DIP Uyumlu: Somut sınıf yerine Interface
    private final AlertPublisher alertPublisher;
    private final IncidentPriorityQueue priorityQueue;

    public SIEMEngine(LogFilter filterChain, IRepository<Incident> repository) {
        this.filterChain = filterChain;
        this.repository = repository;
        this.alertPublisher = new AlertPublisher();
        this.priorityQueue = new IncidentPriorityQueue();

        if (repository != null) {
            List<Incident> saved = repository.loadAll();
            for (Incident inc : saved) {
                priorityQueue.addIncident(inc);
            }
        }
    }

    public void processLog(LogEntry log) {
        if (filterChain != null) {
            Incident incident = filterChain.process(log);
            if (incident != null) {
                priorityQueue.addIncident(incident);
                alertPublisher.notifyObservers(incident);
            }
        }
    }

    public AlertPublisher getAlertPublisher() {
        return alertPublisher;
    }

    public IncidentPriorityQueue getPriorityQueue() {
        return priorityQueue;
    }

    public void shutdown() {
        if (repository != null) {
            repository.saveAll(priorityQueue.getAllIncidentsOrdered());
        }
    }
}