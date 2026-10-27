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
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Test
    public void testIncidentCreationAndState() {
        Incident incident = new Incident("INC-001", "Unauthorized Access", "192.168.1.50", SeverityLevel.HIGH, 75);
        assertEquals("INC-001", incident.getId());
        assertEquals("OPEN", incident.getStatus());
        assertNotNull(incident.getTimestamp());
    }

    @Test
    public void testIpBlacklistFilter() {
        IpBlacklistFilter filter = new IpBlacklistFilter();
        filter.addToBlacklist("10.0.0.5");
        LogEntry log = new LogEntry("10.0.0.5", "SSH Access Attempt");

        Incident incident = filter.process(log);
        assertNotNull(incident);
        assertEquals(SeverityLevel.CRITICAL, incident.getSeverity());
    }

    @Test
    public void testPriorityQueueOrdering() {
        IncidentPriorityQueue queue = new IncidentPriorityQueue();
        Incident incLow = new Incident("INC-1", "Low Risk", "10.0.0.1", SeverityLevel.LOW, 20);
        Incident incHigh = new Incident("INC-2", "High Risk", "10.0.0.2", SeverityLevel.HIGH, 90);

        queue.addIncident(incLow);
        queue.addIncident(incHigh);

        assertEquals(incHigh, queue.pollIncident());
    }

    @Test
    public void testObserverSubscription() {
        AlertPublisher publisher = new AlertPublisher();
        AnalystNotifier notifier = new AnalystNotifier("Security Analyst");
        publisher.attach(notifier);

        Incident incident = new Incident("INC-100", "Test Alert", "127.0.0.1", SeverityLevel.MEDIUM, 50);
        publisher.notifyObservers(incident);

        // Observer deseninin çalıştığının somut kanıtları:
        assertEquals(1, notifier.getNotificationCount());
        assertNotNull(notifier.getLastNotifiedIncident());
        assertEquals("INC-100", notifier.getLastNotifiedIncident().getId());
    }

    @Test
    public void testFileRepositorySaveAndLoadWithStatus() {
        FileRepository repo = new FileRepository();
        Incident inc = new Incident("INC-777", "Persistence Check", "172.16.0.1", SeverityLevel.HIGH, 85);
        inc.setStatus("RESOLVED");

        List<Incident> list = new ArrayList<>();
        list.add(inc);
        repo.saveAll(list);

        List<Incident> loaded = repo.loadAll();
        assertFalse(loaded.isEmpty());
        assertEquals("RESOLVED", loaded.get(0).getStatus());
    }

    @Test
    public void testSafeLogReturnsNull() {
        IpBlacklistFilter filter = new IpBlacklistFilter();
        LogEntry safeLog = new LogEntry("192.168.1.10", "User Login Successful");
        assertNull(filter.process(safeLog));
    }

    @Test
    public void testBruteForceFilterThresholdBehavior() {
        BruteForceFilter filter = new BruteForceFilter();
        LogEntry log1 = new LogEntry("192.168.1.200", "AUTH_FAILURE");
        LogEntry log2 = new LogEntry("192.168.1.200", "AUTH_FAILURE");
        LogEntry log3 = new LogEntry("192.168.1.200", "AUTH_FAILURE");

        assertNull(filter.process(log1));
        assertNull(filter.process(log2));

        Incident incident = filter.process(log3);
        assertNotNull(incident);
        assertEquals(SeverityLevel.CRITICAL, incident.getSeverity());
    }

    @Test
    public void testSIEMEngineLifecycleAndPersistence() {
        FileRepository repo = new FileRepository();
        IpBlacklistFilter filter = new IpBlacklistFilter();
        filter.addToBlacklist("192.168.1.99");

        SIEMEngine engine = new SIEMEngine(filter, repo);
        LogEntry log = new LogEntry("192.168.1.99", "Blocked Traffic");
        engine.processLog(log);
        engine.shutdown();

        List<Incident> saved = repo.loadAll();
        assertFalse(saved.isEmpty());
    }

    @Test
    public void testStrictPersistenceAndTimestampIntegrity() {
        FileRepository repo = new FileRepository();
        LocalDateTime originalTime = LocalDateTime.now().minusHours(2).withNano(0);
        Incident inc = new Incident("INC-999", "Test Persistence", "10.0.0.1", SeverityLevel.HIGH, 80, "RESOLVED", originalTime);

        List<Incident> list = new ArrayList<>();
        list.add(inc);
        repo.saveAll(list);

        List<Incident> loaded = repo.loadAll();
        assertFalse(loaded.isEmpty());
        Incident loadedInc = loaded.get(0);

        assertEquals("RESOLVED", loadedInc.getStatus());
        assertEquals(originalTime, loadedInc.getTimestamp());
    }
}