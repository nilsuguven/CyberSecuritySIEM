package service;

import model.Incident;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

public class IncidentPriorityQueue {
    private final PriorityQueue<Incident> queue;

    public IncidentPriorityQueue() {
        this.queue = new PriorityQueue<>((a, b) -> Integer.compare(b.getThreatScore(), a.getThreatScore()));
    }

    public void addIncident(Incident incident) {
        if (incident != null) {
            queue.add(incident);
        }
    }

    public Incident pollIncident() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public List<Incident> getAllIncidentsOrdered() {
        List<Incident> list = new ArrayList<>(queue);
        list.sort((a, b) -> Integer.compare(b.getThreatScore(), a.getThreatScore()));
        return list;
    }
}