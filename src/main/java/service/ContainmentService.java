package service;

import java.util.HashSet;
import java.util.Set;

public class ContainmentService {
    private final Set<String> blockedIps = new HashSet<>();


    public void blockIp(String ip) {
        if (ip == null || ip.isBlank()) {
            throw new IllegalArgumentException(
                    "IP adresi boş veya null olamaz.");
        }

        boolean added = blockedIps.add(ip);

        if (added) {
            System.out.println(
                    "[CONTAINMENT] IP adresi aktif olarak engellendi (Firewall Rule): "
                            + ip);
        }
    }

    public boolean isIpBlocked(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }

        return blockedIps.contains(ip);
    }

    public Set<String> getBlockedIps() {
        return Set.copyOf(blockedIps);
    }
}