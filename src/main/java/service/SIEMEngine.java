
package service;

import filter.LogFilter;
import model.Incident;
import model.LogEntry;
import observer.AlertPublisher;
import repository.IRepository;

import java.util.ArrayList;
import java.util.List;

public class SIEMEngine {
    private final LogFilter filterChain;
    private final IRepository<Incident> repository;
    private final AlertPublisher alertPublisher;
    private final IncidentPriorityQueue priorityQueue;
    private final ContainmentService containmentService;
    private final IncidentResponseService responseService;

    // Kuyruktan çıkarılsa bile tüm olayları bellekte tutar.
    private final List<Incident> allIncidents = new ArrayList<>();

    public SIEMEngine(LogFilter filterChain, IRepository<Incident> repository) {
        this.filterChain = filterChain;
        this.repository = repository;
        this.alertPublisher = new AlertPublisher();
        this.priorityQueue = new IncidentPriorityQueue();
        this.containmentService = new ContainmentService();
        this.responseService =
                new IncidentResponseService(this.containmentService, repository);

        // Önceki çalıştırmada kaydedilmiş olayları yükle.
        List<Incident> savedIncidents = repository.loadAll();

        for (Incident incident : savedIncidents) {
            allIncidents.add(incident);

            if (!"RESOLVED".equalsIgnoreCase(incident.getStatus())) {
                priorityQueue.addIncident(incident);
            }
        }
    }
    public void processLog(LogEntry log) {
        if (containmentService.isIpBlocked(log.getSourceIp())) {
            System.out.println(
                    "[SIEM] Engellenmiş IP'den gelen log reddedildi: "
                            + log.getSourceIp());
            return;
        }

        Incident incident = filterChain.process(log);

        if (incident != null) {
            priorityQueue.addIncident(incident);
            allIncidents.add(incident);
            alertPublisher.notifyObservers(incident);
        }
    }

    public ContainmentService getContainmentService() {
        return containmentService;
    }

    public IncidentResponseService getResponseService() {
        return responseService;
    }

    public AlertPublisher getAlertPublisher() {
        return alertPublisher;
    }

    public IncidentPriorityQueue getPriorityQueue() {
        return priorityQueue;
    }

    public void shutdown() {
        repository.saveAll(new ArrayList<>(allIncidents));
        System.out.println(
                "[SIEM Engine] Tüm olaylar repository'ye kaydedildi.");
    }
}
