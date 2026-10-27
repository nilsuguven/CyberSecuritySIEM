package filter;

import model.Incident;
import model.LogEntry;
import model.SeverityLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BruteForceFilter extends AbstractLogFilter {
    private final Map<String, List<Long>> failureTimestamps = new HashMap<>();
    private static final int THRESHOLD = 3;
    private static final long TIME_WINDOW_MS = 60000; // 60 saniye

    @Override
    public Incident process(LogEntry log) {
        String msg = log.getMessage().toLowerCase();

        // Sadece oturum açma başarısızlıklarını yakala
        if (msg.contains("failed login") || msg.contains("login failed")) {
            long currentTime = System.currentTimeMillis();
            List<Long> timestamps = failureTimestamps.computeIfAbsent(log.getSourceIp(), k -> new ArrayList<>());

            // 60 saniyeden eski denemeleri listeden temizle (Sliding Window)
            timestamps.removeIf(timestamp -> (currentTime - timestamp) > TIME_WINDOW_MS);
            timestamps.add(currentTime);

            // Sadece 3. denemede (eşik ilk kez aşıldığında) 1 kere olay üret
            if (timestamps.size() == THRESHOLD) {
                return new Incident(
                        "INC-BF-" + currentTime,
                        "Brute Force Attack Detected",
                        log.getSourceIp(),
                        SeverityLevel.HIGH,
                        80
                );
            }
        }

        if (nextFilter != null) {
            return nextFilter.process(log);
        }
        return null;
    }
}