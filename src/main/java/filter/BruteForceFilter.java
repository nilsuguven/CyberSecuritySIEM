package filter;

import model.Incident;
import model.LogEntry;
import model.SeverityLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BruteForceFilter extends AbstractLogFilter {
    // IP başına başarısız denemelerin milisaniye cinsinden zaman damgaları
    private final Map<String, List<Long>> failureTimestamps = new HashMap<>();
    private static final int THRESHOLD = 3;
    private static final long TIME_WINDOW_MS = 60 * 1000; // 60 saniye (1 dakika)

    @Override
    public Incident process(LogEntry log) {
        if (log != null && log.getMessage() != null && log.getMessage().toUpperCase().contains("AUTH_FAILURE")) {
            String ip = log.getSourceIp();
            long currentTime = System.currentTimeMillis();

            // IP için liste yoksa oluştur
            failureTimestamps.putIfAbsent(ip, new ArrayList<>());
            List<Long> timestamps = failureTimestamps.get(ip);

            // 1. Zaman penceresi dışındaki (60 saniyeden eski) denemeleri temizle
            timestamps.removeIf(timestamp -> (currentTime - timestamp) > TIME_WINDOW_MS);

            // 2. Yeni başarısız denemenin zamanını ekle
            timestamps.add(currentTime);

            // 3. Yalnızca eşik değere (tam 3 denemeye) ULAŞILDIĞI AN olay tetikle
            if (timestamps.size() == THRESHOLD) {
                return new Incident(
                        "INC-" + currentTime,
                        "Brute Force Attack Detected",
                        ip,
                        SeverityLevel.CRITICAL,
                        90
                );
            }
        }

        if (nextFilter != null) {
            return nextFilter.process(log);
        }
        return null;
    }
}