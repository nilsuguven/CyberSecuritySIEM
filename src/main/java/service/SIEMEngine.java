package service;

import filter.AbstractLogFilter;
import filter.BruteForceFilter;
import filter.IpBlacklistFilter;
import model.Incident;
import model.LogEntry;
import observer.AlertPublisher;
import repository.FileRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class SIEMEngine {
    private final AbstractLogFilter filterChain;
    private final PriorityQueue<Incident> priorityQueue;
    private final AlertPublisher alertPublisher;
    private final FileRepository repository;
    private final List<Incident> resolvedIncidents;

    public SIEMEngine(AbstractLogFilter filterChain, FileRepository repository) {
        // Eğer dışarıdan filtre zinciri verilmediyse varsayılan öncelikli zinciri kur:
        // BruteForceFilter (CRITICAL) -> IpBlacklistFilter (HIGH)
        if (filterChain == null) {
            BruteForceFilter bf = new BruteForceFilter();
            IpBlacklistFilter ip = new IpBlacklistFilter();
            bf.setNextFilter(ip);
            this.filterChain = bf;
        } else {
            this.filterChain = filterChain;
        }

        this.repository = repository;
        // PriorityQueue Max-Heap sıralaması: Incident'ın threatScore değerine göre
        this.priorityQueue = new PriorityQueue<>((a, b) -> Integer.compare(b.getThreatScore(), a.getThreatScore()));
        this.alertPublisher = new AlertPublisher();
        this.resolvedIncidents = new ArrayList<>();

        if (this.repository != null) {
            loadPersistedData();
        }
    }

    public Incident processLog(LogEntry log) {
        if (filterChain == null) return null;

        Incident incident = filterChain.process(log);
        if (incident != null) {
            priorityQueue.add(incident);
            // Tehdit skoru 80 ve üzeri olan (CRITICAL) olaylarda bildirim gönder
            if (incident.getThreatScore() >= 80) {
                alertPublisher.notifyObservers(incident);
            }
        }
        return incident;
    }

    public void resolveIncident(Incident incident) {
        if (incident != null) {
            incident.setStatus("RESOLVED");
            resolvedIncidents.add(incident);
        }
    }

    private void loadPersistedData() {
        List<Incident> loaded = repository.loadAll();
        if (loaded != null) {
            for (Incident incident : loaded) {
                if ("RESOLVED".equalsIgnoreCase(incident.getStatus())) {
                    resolvedIncidents.add(incident);
                } else {
                    priorityQueue.add(incident);
                }
            }
        }
    }

    public void shutdown() {
        if (repository != null) {
            List<Incident> allIncidents = new ArrayList<>();
            allIncidents.addAll(priorityQueue);
            allIncidents.addAll(resolvedIncidents);
            repository.saveAll(allIncidents);
        }
    }

    public PriorityQueue<Incident> getPriorityQueue() {
        return priorityQueue;
    }

    public AlertPublisher getAlertPublisher() {
        return alertPublisher;
    }

    public List<Incident> getResolvedIncidents() {
        return resolvedIncidents;
    }
}