package filter;

import model.Incident;
import model.LogEntry;
import model.SeverityLevel;
import java.util.HashSet;
import java.util.Set;

public class IpBlacklistFilter extends AbstractLogFilter {
    private final Set<String> blacklistedIps = new HashSet<>();
    private LogFilter nextFilter;

    public void addToBlacklist(String ip) {
        blacklistedIps.add(ip);
    }

    public void setNext(LogFilter nextFilter) {
        this.nextFilter = nextFilter;
    }

    @Override
    public Incident process(LogEntry log) {
        if (log != null && blacklistedIps.contains(log.getSourceIp())) {
            return new Incident("INC-" + System.currentTimeMillis(), "Blacklisted IP Access Attempt", log.getSourceIp(), SeverityLevel.CRITICAL, 95);
        }
        if (nextFilter != null) {
            return nextFilter.process(log);
        }
        return null;
    }
}