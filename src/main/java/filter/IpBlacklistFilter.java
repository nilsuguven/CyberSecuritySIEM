package filter;

import model.Incident;
import model.LogEntry;
import model.SeverityLevel;
import java.util.Arrays;
import java.util.List;

public class IpBlacklistFilter extends AbstractLogFilter {
    // Örnek karalistedeki zararlı IP'ler
    private final List<String> blacklistedIps = Arrays.asList("192.168.1.100", "10.0.0.66", "45.33.22.11");

    @Override
    public Incident process(LogEntry log) {
        if (blacklistedIps.contains(log.getSourceIp())) {
            return new Incident(
                    "INC-" + System.currentTimeMillis() % 10000,
                    "Karalistedeki IP Bağlantı Tespit Edildi (" + log.getLogType() + ")",
                    log.getSourceIp(),
                    SeverityLevel.HIGH,
                    85 // Yüksek tehdit skoru
            );
        }
        return checkNext(log);
    }
}