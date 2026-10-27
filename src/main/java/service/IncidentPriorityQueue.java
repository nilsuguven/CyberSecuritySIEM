package service;

import model.Incident;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class IncidentPriorityQueue {
    private final PriorityQueue<Incident> priorityQueue;

    public IncidentPriorityQueue() {
        // En yüksek tehdit puanına (threatScore) göre önceliklendir
        this.priorityQueue = new PriorityQueue<>((a, b) -> Integer.compare(b.getThreatScore(), a.getThreatScore()));
    }

    public void addIncident(Incident incident) {
        priorityQueue.add(incident);
    }

    public Incident pollIncident() {
        return priorityQueue.poll();
    }

    public boolean isEmpty() {
        return priorityQueue.isEmpty();
    }

    // Kuyruktaki olaylari silmeden, sirali bir liste halinde dondurur
    public List<Incident> getAllIncidentsOrdered() {
        List<Incident> list = new ArrayList<>(priorityQueue);
        list.sort((a, b) -> Integer.compare(b.getThreatScore(), a.getThreatScore()));
        return list;
    }
}