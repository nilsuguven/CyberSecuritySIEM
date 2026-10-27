package filter;

import model.Incident;
import model.LogEntry;
import model.SeverityLevel;

import java.util.HashSet;
import java.util.Set;

public class IpBlacklistFilter extends AbstractLogFilter {
    private final Set<String> blacklistedIps = new HashSet<>();

    public void addToBlacklist(String ip) {
        blacklistedIps.add(ip);
    }

    @Override
    public Incident process(LogEntry log) {
        if (blacklistedIps.contains(log.getSourceIp())) {
            return new Incident(
                    "INC-BLK-" + System.currentTimeMillis(),
                    "Blacklisted IP Access Attempt",
                    log.getSourceIp(),
                    SeverityLevel.CRITICAL,
                    100
            );
        }

        if (nextFilter != null) {
            return nextFilter.process(log);
        }

        return null;
    }
}