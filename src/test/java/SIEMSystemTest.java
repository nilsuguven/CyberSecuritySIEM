import filter.BruteForceFilter;
import filter.IpBlacklistFilter;
import model.Incident;
import model.LogEntry;
import model.SeverityLevel;
import observer.AlertPublisher;
import observer.AnalystNotifier;
import repository.FileRepository;
import service.ContainmentService;
import service.IncidentPriorityQueue;
import service.SIEMEngine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SIEMSystemTest {

    @BeforeEach
    public void setUp() {
        File file = new File("incidents.txt");
        if (file.exists()) {
            file.delete();
        }
    }

    // Test 1: Observer Deseni Bildirim Testi
    @Test
    public void testObserverNotification() {
        AlertPublisher publisher = new AlertPublisher();
        AnalystNotifier analyst = new AnalystNotifier("SOC Analyst 1");

        publisher.attach(analyst);

        Incident incident = new Incident("INC-101", "Unauthorized Access", "192.168.1.10", SeverityLevel.HIGH, 85);
        publisher.notifyObservers(incident);

        assertEquals(1, analyst.getNotificationCount());
        assertNotNull(analyst.getLastNotifiedIncident());
        assertEquals("INC-101", analyst.getLastNotifiedIncident().getId());
    }

    // Test 2: Incident Yasam Dongusu ve Containment Akisi
    @Test
    public void testIncidentLifecycleAndContainmentFlow() {
        FileRepository repo = new FileRepository();
        IpBlacklistFilter filter = new IpBlacklistFilter();
        SIEMEngine engine = new SIEMEngine(filter, repo);

        Incident incident = new Incident("INC-555", "Containment Check", "10.0.0.99", SeverityLevel.CRITICAL, 95);

        assertEquals("OPEN", incident.getStatus());

        engine.getResponseService().updateIncidentStatus(incident, "IN_PROGRESS");
        assertEquals("IN_PROGRESS", incident.getStatus());

        engine.getResponseService().updateIncidentStatus(incident, "CONTAINED");
        assertEquals("CONTAINED", incident.getStatus());
        assertTrue(engine.getContainmentService().isIpBlocked("10.0.0.99"));

        engine.getResponseService().updateIncidentStatus(incident, "RESOLVED");
        assertEquals("RESOLVED", incident.getStatus());
    }

    // Test 3: BruteForce Esik Mantigi (3. denemede tetiklenme, 4. denemede tekrarlamama)
    @Test
    public void testBruteForceFilterThresholdBehavior() {
        BruteForceFilter filter = new BruteForceFilter();
        String ip = "192.168.1.50";

        LogEntry log1 = new LogEntry(ip, "Failed login attempt 1");
        LogEntry log2 = new LogEntry(ip, "Failed login attempt 2");
        LogEntry log3 = new LogEntry(ip, "Failed login attempt 3");
        LogEntry log4 = new LogEntry(ip, "Failed login attempt 4");

        assertNull(filter.process(log1));
        assertNull(filter.process(log2));

        Incident inc3 = filter.process(log3);
        assertNotNull(inc3);
        assertEquals("Brute Force Attack Detected", inc3.getTitle());

        Incident inc4 = filter.process(log4);
        assertNull(inc4);
    }

    // Test 4: Kara Liste Filtresi Testi
    @Test
    public void testIpBlacklistFilter() {
        IpBlacklistFilter filter = new IpBlacklistFilter();
        filter.addToBlacklist("192.168.1.100");

        LogEntry log = new LogEntry("192.168.1.100", "Normal traffic");
        Incident incident = filter.process(log);

        assertNotNull(incident);
        assertTrue(incident.getTitle().contains("Blacklisted IP"));
        assertEquals(SeverityLevel.CRITICAL, incident.getSeverity());
    }

    // Test 5: Incident Priority Queue (Oncelik Kuyrugu) Testi
    @Test
    public void testIncidentPriorityQueue() {
        IncidentPriorityQueue queue = new IncidentPriorityQueue();

        Incident lowThreat = new Incident("INC-1", "Low", "1.1.1.1", SeverityLevel.LOW, 20);
        Incident highThreat = new Incident("INC-2", "High", "2.2.2.2", SeverityLevel.HIGH, 90);

        queue.addIncident(lowThreat);
        queue.addIncident(highThreat);

        assertEquals(highThreat, queue.pollIncident());
        assertEquals(lowThreat, queue.pollIncident());
    }

    // Test 6: FileRepository Kaydetme ve Geri Yukleme Testi

    @Test
    public void testFileRepositoryPersistence() {
        FileRepository repo = new FileRepository();

        Incident incident = new Incident(
                "INC-999",
                "Disk Full",
                "127.0.0.1",
                SeverityLevel.MEDIUM,
                50
        );

        incident.setStatus("IN_PROGRESS");

        repo.saveAll(Collections.singletonList(incident));

        List<Incident> loadedIncidents = repo.loadAll();

        assertEquals(1, loadedIncidents.size());

        Incident loaded = loadedIncidents.get(0);

        assertEquals("INC-999", loaded.getId());
        assertEquals("Disk Full", loaded.getTitle());
        assertEquals("127.0.0.1", loaded.getSourceIp());
        assertEquals(SeverityLevel.MEDIUM, loaded.getSeverity());
        assertEquals(50, loaded.getThreatScore());
        assertEquals("IN_PROGRESS", loaded.getStatus());
        assertEquals(incident.getTimestamp(), loaded.getTimestamp());
    }

    // Test 7: Containment Service IP Engelleme Testi
    @Test
    public void testContainmentServiceBlocking() {
        ContainmentService containmentService = new ContainmentService();
        assertFalse(containmentService.isIpBlocked("10.0.0.1"));

        containmentService.blockIp("10.0.0.1");
        assertTrue(containmentService.isIpBlocked("10.0.0.1"));
    }

    // Test 8: SIEMEngine Engellenmis IP Log Reddetme Testi
    @Test
    public void testSIEMEngineBlockedIpLogDrop() {
        IpBlacklistFilter filter = new IpBlacklistFilter();
        FileRepository repo = new FileRepository();
        SIEMEngine engine = new SIEMEngine(filter, repo);

        engine.getContainmentService().blockIp("172.16.0.5");
        LogEntry log = new LogEntry("172.16.0.5", "Unauthorized access attempt");

        engine.processLog(log);
        assertTrue(engine.getPriorityQueue().isEmpty());
    }

    // Test 9: Chain of Responsibility (Filtre Zinciri Gecis) Testi
    @Test
    public void testFilterChainDelegation() {
        IpBlacklistFilter blacklistFilter = new IpBlacklistFilter();
        BruteForceFilter bruteForceFilter = new BruteForceFilter();

        blacklistFilter.setNextFilter(bruteForceFilter);

        String ip = "10.0.0.50";

        LogEntry log1 = new LogEntry(ip, "Failed login attempt 1");
        LogEntry log2 = new LogEntry(ip, "Failed login attempt 2");
        LogEntry log3 = new LogEntry(ip, "Failed login attempt 3");

        assertNull(blacklistFilter.process(log1));
        assertNull(blacklistFilter.process(log2));

        Incident incident = blacklistFilter.process(log3);
        assertNotNull(incident);
        assertEquals("Brute Force Attack Detected", incident.getTitle());
    }

    // Test 10: SIEMEngine baslangicta kayitli olaylari yukler
    @Test
    public void testSIEMEngineLoadsSavedIncidentsOnStartup() {
        FileRepository repo = new FileRepository();

        Incident incident = new Incident(
                "INC-RESTORE-1",
                "Saved Incident",
                "10.0.0.20",
                SeverityLevel.HIGH,
                80
        );

        repo.saveAll(Collections.singletonList(incident));

        IpBlacklistFilter filter = new IpBlacklistFilter();
        SIEMEngine engine = new SIEMEngine(filter, repo);

        List<Incident> loaded =
                engine.getPriorityQueue().getAllIncidentsOrdered();

        assertEquals(1, loaded.size());
        assertEquals("INC-RESTORE-1", loaded.get(0).getId());
        assertEquals("Saved Incident", loaded.get(0).getTitle());
    }


}