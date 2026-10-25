package filter;

import model.Incident;
import model.LogEntry;

public abstract class AbstractLogFilter implements LogFilter {
    protected LogFilter nextFilter;

    @Override
    public void setNextFilter(LogFilter nextFilter) {
        this.nextFilter = nextFilter;
    }

    protected Incident checkNext(LogEntry log) {
        if (nextFilter != null) {
            return nextFilter.process(log);
        }
        return null; // Zincirin sonuna gelindi, tehdit bulunamadı
    }
}