import model.Incident;
import model.LogEntry;
import observer.AlertPublisher;
import observer.AnalystNotifier;
import repository.FileRepository;
import service.SIEMEngine;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // 1. Observer / Bildirim Yayıncısını Başlatma
        AlertPublisher alertPublisher = new AlertPublisher();
        AnalystNotifier SOCNotifier = new AnalystNotifier("SOC Ekibi");
        alertPublisher.subscribe(SOCNotifier);

        // 2. SIEM Motoru ve Veri Depolama Servisini Başlatma
        SIEMEngine siemEngine = new SIEMEngine(alertPublisher);
        FileRepository repository = new FileRepository();

        // Daha önce dosyada kayıtlı olaylar varsa yükle
        List<Incident> savedIncidents = repository.loadAll();
        for (Incident inc : savedIncidents) {
            siemEngine.getPriorityQueue().addIncident(inc);
        }

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("=================================================");
        System.out.println("   SİBER GÜVENLİK OLAY YÖNETİM SİSTEMİ (SIEM)    ");
        System.out.println("=================================================");

        while (running) {
            System.out.println("\n--- MENÜ ---");
            System.out.println("1. Yeni Log Girişi Simüle Et (Kural Motorunu Tetikle)");
            System.out.println("2. Aktif Güvenlik Olaylarını İncele (Öncelikli Heap Sıralaması)");
            System.out.println("3. En Kritik Olayı Çek ve Müdahale Et (Poll Queue)");
            System.out.println("4. Olayları Kalıcı Dosyaya Kaydet");
            System.out.println("0. Çıkış");
            System.out.print("Seçiminiz: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Kaynak IP Giriniz (örn: 192.168.1.100 veya 10.0.0.5): ");
                    String ip = scanner.nextLine();
                    System.out.print("Log Türü Giriniz (AUTH_FAILURE, PORT_SCAN, NORMAL): ");
                    String type = scanner.nextLine();

                    LogEntry log = new LogEntry("LOG-" + System.currentTimeMillis() % 1000, ip, "Sistem Olayı", type);
                    Incident detectedIncident = siemEngine.processLog(log);

                    if (detectedIncident != null) {
                        System.out.println("\n[!] SÜZGEÇ TETİKLENDİ: Yeni güvenlik olayı oluşturuldu ve öncelik kuyruğuna eklendi!");
                        System.out.println("Tespit Edilen Olay: " + detectedIncident);
                    } else {
                        System.out.println("\n[+] Log güvenli. Herhangi bir tehdit süzgecine takılmadı.");
                    }
                    break;

                case "2":
                    System.out.println("\n=== ÖNCELİKLENDİRİLMİŞ GÜVENLİK OLAYLARI (Max-Heap Sıralı) ===");
                    List<Incident> incidents = siemEngine.getPriorityQueue().getAllIncidentsOrdered();
                    if (incidents.isEmpty()) {
                        System.out.println("Sistemde aktif güvenlik olayı bulunmuyor.");
                    } else {
                        for (Incident inc : incidents) {
                            System.out.println(inc);
                        }
                    }
                    break;

                case "3":
                    Incident topIncident = siemEngine.getPriorityQueue().pollNextIncident();
                    if (topIncident != null) {
                        System.out.println("\n[MÜDAHALE EDİLİYOR] En Yüksek Tehdit Skoru Olan Olay Kuyruktan Çekildi:");
                        System.out.println(topIncident);
                        topIncident.setStatus("RESOLVED");
                        System.out.println("Olay Durumu 'RESOLVED' Yapıldı.");
                    } else {
                        System.out.println("Müdahale edilecek olay bulunmuyor.");
                    }
                    break;

                case "4":
                    repository.saveAll(siemEngine.getPriorityQueue().getAllIncidentsOrdered());
                    System.out.println("\n[KAYIT] Tüm olaylar 'incidents.txt' dosyasına başarıyla kaydedildi.");
                    break;

                case "0":
                    repository.saveAll(siemEngine.getPriorityQueue().getAllIncidentsOrdered());
                    System.out.println("Sistem kapatılıyor. Veriler kaydedildi.");
                    running = false;
                    break;

                default:
                    System.out.println("Geçersiz seçim, tekrar deneyiniz.");
            }
        }
        scanner.close();
    }
}