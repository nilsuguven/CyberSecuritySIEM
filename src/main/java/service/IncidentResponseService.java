
package service;

import model.Incident;
import repository.IRepository;

import java.util.List;
import java.util.Locale;

public class IncidentResponseService {

    private final ContainmentService containmentService;
    private final IRepository<Incident> repository;

    public IncidentResponseService(
            ContainmentService containmentService,
            IRepository<Incident> repository) {
        this.containmentService = containmentService;
        this.repository = repository;
    }

    // İzin verilen durum akışı:
    // OPEN -> IN_PROGRESS -> CONTAINED -> RESOLVED
    public void updateIncidentStatus(Incident incident, String newStatus) {

        if (incident == null) {
            throw new IllegalArgumentException(
                    "Olay nesnesi null olamaz.");
        }

        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException(
                    "Yeni durum boş veya null olamaz.");
        }

        String oldStatus = incident.getStatus();

        if (oldStatus == null || oldStatus.isBlank()) {
            throw new IllegalStateException(
                    "Mevcut olay durumu geçersiz.");
        }

        String normalizedOldStatus =
                oldStatus.trim().toUpperCase(Locale.ROOT);

        String normalizedNewStatus =
                newStatus.trim().toUpperCase(Locale.ROOT);

        boolean validTransition = switch (normalizedOldStatus) {
            case "OPEN" -> normalizedNewStatus.equals("IN_PROGRESS");
            case "IN_PROGRESS" -> normalizedNewStatus.equals("CONTAINED");
            case "CONTAINED" -> normalizedNewStatus.equals("RESOLVED");
            default -> false;
        };

        if (!validTransition) {
            throw new IllegalArgumentException(
                    "Geçersiz durum geçişi: "
                            + normalizedOldStatus + " -> "
                            + normalizedNewStatus);
        }

        // IP engelleme başarısız olursa olay durumunu değiştirme.
        if (normalizedNewStatus.equals("CONTAINED")) {
            containmentService.blockIp(incident.getSourceIp());
        }

        incident.setStatus(normalizedNewStatus);

        System.out.println(
                "[RESPONSE] Olay " + incident.getId()
                        + " durumu güncellendi: "
                        + normalizedOldStatus + " -> "
                        + normalizedNewStatus);
    }

    public void saveCurrentState(List<Incident> incidents) {
        repository.saveAll(incidents);
    }
}
