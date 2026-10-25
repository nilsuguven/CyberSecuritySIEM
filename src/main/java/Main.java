import model.Incident;
import model.LogEntry;
import observer.AnalystNotifier;
import repository.FileRepository;
import service.SIEMEngine;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // 1. Veri Depolama ve SIEM Motorunu Başlatma
        FileRepository repository = new FileRepository();

        // SIEMEngine açılışta kayıtlı verileri otomatik yükler
        SIEMEngine siemEngine = new SIEMEngine(null, repository);

        // 2. Observer / Bildirim Yayıncısını Bağlama
        AnalystNotifier SOCNotifier = new AnalystNotifier("SOC Ekibi");
        siemEngine.getAlertPublisher().subscribe(SOCNotifier);

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
                    if (siemEngine.getPriorityQueue().isEmpty()) {
                        System.out.println("Sistemde aktif güvenlik olayı bulunmuyor.");
                    } else {
                        for (Incident inc : siemEngine.getPriorityQueue()) {
                            System.out.println(inc);
                        }
                    }
                    break;

                case "3":
                    Incident topIncident = siemEngine.getPriorityQueue().poll();
                    if (topIncident != null) {
                        siemEngine.resolveIncident(topIncident);
                        System.out.println("Olay çözüldü ve arşivlendi: " + topIncident);
                    } else {
                        System.out.println("Çözülecek aktif olay yok.");
                    }
                    break;

                case "4":
                    siemEngine.shutdown();
                    System.out.println("\n[KAYIT] Tüm olaylar (aktif ve çözülenler) 'incidents.txt' dosyasına başarıyla kaydedildi.");
                    break;

                case "0":
                    running = false;
                    break;

                default:
                    System.out.println("Geçersiz seçim, tekrar deneyiniz.");
            }
        }

        // Çıkışta otomatik kaydetme ve temiz kapatma
        siemEngine.shutdown();
        System.out.println("Tüm veriler (aktif ve çözülenler) kaydedildi. Sistem kapatılıyor.");
        scanner.close();
    }
}