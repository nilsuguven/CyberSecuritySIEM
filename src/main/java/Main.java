import filter.BruteForceFilter;
import filter.IpBlacklistFilter;
import model.Incident;
import model.LogEntry;
import observer.AnalystNotifier;
import repository.FileRepository;
import repository.IRepository;
import service.IncidentPriorityQueue;
import service.SIEMEngine;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 1. Veri Depolama ve SIEM Motorunu Başlatma (DIP Uyumlu)
        IRepository<Incident> repository = new FileRepository();

        IpBlacklistFilter blacklistFilter = new IpBlacklistFilter();
        blacklistFilter.addToBlacklist("192.168.1.100");

        BruteForceFilter bruteForceFilter = new BruteForceFilter();
        blacklistFilter.setNext(bruteForceFilter);

        SIEMEngine siemEngine = new SIEMEngine(blacklistFilter, repository);

        // 2. Observer / Bildirim Yayıncısını Bağlama
        AnalystNotifier SOCNotifier = new AnalystNotifier("SOC Analyst");
        siemEngine.getAlertPublisher().attach(SOCNotifier);

        // Örnek Log İşlemleri
        System.out.println("--- Log İşleme Başlatılıyor ---");
        siemEngine.processLog(new LogEntry("192.168.1.100", "Karaslistedeki IP Erişimi"));
        siemEngine.processLog(new LogEntry("10.0.0.5", "Normal Kullanıcı Girişi"));

        // Öncelik Kuyruğundaki Olayları Listeleme (Sıralı Gösterim)
        IncidentPriorityQueue queue = siemEngine.getPriorityQueue();
        List<Incident> sortedIncidents = queue.getAllIncidentsOrdered();

        System.out.println("\n--- Öncelikli Olay Listesi ---");
        for (Incident inc : sortedIncidents) {
            System.out.println(inc.getId() + " - " + inc.getTitle() + " - Skor: " + inc.getThreatScore());
        }

        // En Yüksek Öncelikli Olayı Çekme
        if (!queue.isEmpty()) {
            Incident topIncident = queue.pollIncident();
            System.out.println("\nİşlenen En Yüksek Öncelikli Olay: " + topIncident.getId());
        }

        // Sistemi Kapatma ve Verileri Kaydetme
        siemEngine.shutdown();
        System.out.println("\nSIEM Motoru kapatıldı, veriler kaydedildi.");
    }
}