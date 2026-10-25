package filter;

import model.Incident;
import model.LogEntry;
import model.SeverityLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BruteForceFilter extends AbstractLogFilter {
    private final Map<String, List<Long>> failureTracker = new HashMap<>();
    private static final int THRESHOLD = 3;
    private static final long TIME_WINDOW_MS = 60000;

    @Override
    public Incident process(LogEntry log) {
        if ("AUTH_FAILURE".equalsIgnoreCase(log.getLogType())) {
            String ip = log.getSourceIp();
            long currentTime = System.currentTimeMillis();

            failureTracker.putIfAbsent(ip, new ArrayList<>());
            List<Long> timestamps = failureTracker.get(ip);

            timestamps.removeIf(time -> (currentTime - time) > TIME_WINDOW_MS);
            timestamps.add(currentTime);

            if (timestamps.size() >= THRESHOLD) {
                timestamps.clear();
                return new Incident(
                        "INC-BF-" + (currentTime % 10000),
                        "Brute Force Saldırısı Tespit Edildi! (" + THRESHOLD + " başarısız deneme)",
                        ip,
                        SeverityLevel.CRITICAL,
                        90 // Threat Score (Kritik olay puanı)
                );
            }
        }

        return checkNext(log);
    }
}