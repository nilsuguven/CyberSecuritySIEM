package service;

import model.Incident;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.List;

public class IncidentPriorityQueue {
    // Java'nın PriorityQueue yapısı varsayılan olarak min-heap çalışır,
    // ancak Incident sınıfında compareTo metodunu tersten yazdığımız için
    // burada en yüksek threatScore'a sahip olay en üstte (Max-Heap) durur.
    private final PriorityQueue<Incident> queue = new PriorityQueue<>();

    public void addIncident(Incident incident) {
        queue.add(incident);
    }

    public Incident pollNextIncident() {
        return queue.poll(); // En yüksek tehdit puanlı olayı çeker ve kuyruktan çıkarır
    }

    public Incident peekNextIncident() {
        return queue.peek(); // En yüksek tehdit puanlı olayı silmeden görüntüler
    }

    public List<Incident> getAllIncidentsOrdered() {
        List<Incident> sortedList = new ArrayList<>(queue);
        sortedList.sort(null); // Tehdit skoru en yüksekten en düşüğe sıralar
        return sortedList;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }
}