import filter.AbstractLogFilter;
import filter.BruteForceFilter;
import filter.IpBlacklistFilter;
import model.Incident;
import model.LogEntry;
import model.SeverityLevel;
import observer.AlertPublisher;
import observer.AnalystNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.FileRepository;
import service.IncidentPriorityQueue;
import service.SIEMEngine;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SIEMSystemTest {

    private IncidentPriorityQueue priorityQueue;
    private AlertPublisher alertPublisher;
    private SIEMEngine siemEngine;
    private FileRepository repository;

    @BeforeEach
    void setUp() {
        priorityQueue = new IncidentPriorityQueue();
        alertPublisher = new AlertPublisher();
        repository = new FileRepository();
        siemEngine = new SIEMEngine(null, repository);
    }

    // TEST 1: Incident modelinin durum (OPEN) ve zaman damgası doğrulaması
    @Test
    void testIncidentCreationAndState() {
        Incident incident = new Incident("INC-1", "Test Olayı", "10.0.0.1", SeverityLevel.HIGH, 80);
        assertEquals("INC-1", incident.getId());
        assertEquals(80, incident.getThreatScore());
        assertEquals("OPEN", incident.getStatus(), "Yeni oluşturulan olay OPEN durumunda olmalı");
        assertNotNull(incident.getTimestamp(), "Olay zaman damgası boş olmamalı");
    }

    // TEST 2: IpBlacklistFilter karaliste IP tespiti ve seviye kontrolü
    @Test
    void testIpBlacklistFilter() {
        IpBlacklistFilter filter = new IpBlacklistFilter();
        LogEntry log = new LogEntry("L1", "192.168.1.100", "Şüpheli İstek", "NORMAL");
        Incident incident = filter.process(log);

        assertNotNull(incident);
        assertEquals(SeverityLevel.HIGH, incident.getSeverity());
        assertEquals("192.168.1.100", incident.getSourceIp());
    }

    // TEST 3: Max-Heap Öncelik Kuyruğunun yüksek skorlu olayı öne alması
    @Test
    void testPriorityQueueOrdering() {
        Incident lowThreat = new Incident("INC-1", "Düşük Tehdit", "1.1.1.1", SeverityLevel.LOW, 30);
        Incident highThreat = new Incident("INC-2", "Yüksek Tehdit", "2.2.2.2", SeverityLevel.CRITICAL, 95);

        priorityQueue.addIncident(lowThreat);
        priorityQueue.addIncident(highThreat);

        assertEquals("INC-2", priorityQueue.pollNextIncident().getId(), "En yüksek skorlu olay ilk sırada çıkmalı");
    }

    // TEST 4: Observer Pattern bildirim tetikleme testi
    @Test
    void testObserverSubscription() {
        AnalystNotifier notifier = new AnalystNotifier("Test SOC");
        alertPublisher.subscribe(notifier);

        Incident criticalIncident = new Incident("INC-99", "Kritik Saldırı", "1.1.1.1", SeverityLevel.CRITICAL, 99);
        assertDoesNotThrow(() -> alertPublisher.notifyObservers(criticalIncident));
    }

    // TEST 5: Çözülen (RESOLVED) Olayların Durum ve Dosya Kalıcılığı Testi
    @Test
    void testFileRepositorySaveAndLoadWithStatus() {
        Incident incident = new Incident("INC-TEST", "Kalıcı Kayıt", "127.0.0.1", SeverityLevel.MEDIUM, 50);
        incident.setStatus("RESOLVED"); // Durum güncellemesi

        repository.saveAll(List.of(incident));

        List<Incident> loaded = repository.loadAll();
        assertFalse(loaded.isEmpty(), "Dosyadan en az 1 olay yüklenmeli");
        assertEquals("RESOLVED", loaded.get(0).getStatus(), "Yüklenen olayın durumu RESOLVED olarak korunmalı");
        assertEquals("127.0.0.1", loaded.get(0).getSourceIp());
    }

    // TEST 6: Güvenli log girişinde olay tetiklenmemesi
    @Test
    void testSafeLogReturnsNull() {
        LogEntry safeLog = new LogEntry("L4", "192.168.1.5", "Normal Kullanıcı Girişi", "NORMAL");
        Incident incident = siemEngine.processLog(safeLog);

        assertNull(incident, "Güvenli log için olay tetiklenmemeli");
    }

    // TEST 7: Brute Force Eşik Mekanizması Doğrulaması (Kritik Davranış Testi)
    @Test
    public void testBruteForceFilterThresholdBehavior() {
        AbstractLogFilter bfFilter = new BruteForceFilter();
        FileRepository repo = new FileRepository();
        SIEMEngine engine = new SIEMEngine(bfFilter, repo);

        LogEntry log1 = new LogEntry("LOG-1", "192.168.1.50", "Auth Fail 1", "AUTH_FAILURE");
        LogEntry log2 = new LogEntry("LOG-2", "192.168.1.50", "Auth Fail 2", "AUTH_FAILURE");
        LogEntry log3 = new LogEntry("LOG-3", "192.168.1.50", "Auth Fail 3", "AUTH_FAILURE");

        // 1 ve 2. denemede henüz eşik (3) aşılmadığı için null dönmeli
        assertNull(engine.processLog(log1), "1. başarısız denemede olay oluşmamalı");
        assertNull(engine.processLog(log2), "2. başarısız denemede olay oluşmamalı");

        // 3. denemede tam olarak CRITICAL olay tetiklenmeli
        Incident inc3 = engine.processLog(log3);
        assertNotNull(inc3, "3. denemede eşik aşıldığı için olay tetiklenmeli");
        assertEquals(SeverityLevel.CRITICAL, inc3.getSeverity());
        assertEquals(90, inc3.getThreatScore());
    }

    // TEST 8: SIEMEngine shutdown ve Yaşam Döngüsü Kalıcılığı Testi
    @Test
    public void testSIEMEngineLifecycleAndPersistence() {
        FileRepository repo = new FileRepository();
        SIEMEngine engine = new SIEMEngine(null, repo);

        LogEntry log = new LogEntry("LOG-100", "192.168.1.100", "Blacklist Test", "NORMAL");
        Incident incident = engine.processLog(log);

        if (incident != null) {
            engine.resolveIncident(incident);
            assertEquals("RESOLVED", incident.getStatus());
            assertTrue(engine.getResolvedIncidents().contains(incident));
        }

        // Kapatma simülasyonu (dosyaya yazma)
        assertDoesNotThrow(engine::shutdown);
    }
}