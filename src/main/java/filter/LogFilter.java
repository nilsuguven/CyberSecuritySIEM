package filter;

import model.Incident;
import model.LogEntry;

public interface LogFilter {
    void setNextFilter(LogFilter nextFilter);
    Incident process(LogEntry log);
}