package com.gitlab.mihnea_purcaru1.service_ticketsense.service;

import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.SlaSnapshot;
import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.Status;
import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.Ticket;
import com.gitlab.mihnea_purcaru1.service_ticketsense.repository.SlaSnapshotRepository;
import com.gitlab.mihnea_purcaru1.service_ticketsense.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlaSnapshotService {

    private final TicketRepository ticketRepository;
    private final SlaSnapshotRepository slaSnapshotRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void takeSnapshot() {
        LocalDate today = LocalDate.now();

        if (slaSnapshotRepository.findBySnapshotDate(today).isPresent()) {
            log.info("SLA snapshot for {} already exists, skipping", today);
            return;
        }

        List<Ticket> allTickets = ticketRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        // SLA compliance
        List<Ticket> closedWithEstimate = allTickets.stream()
                .filter(t -> (t.getStatus() == Status.CLOSED || t.getStatus() == Status.RESOLVED)
                        && t.getEstimatedMinutes() != null && t.getClosedAt() != null)
                .toList();

        double compliance = 0.0;
        if (!closedWithEstimate.isEmpty()) {
            long onTime = closedWithEstimate.stream()
                    .filter(t -> ChronoUnit.MINUTES.between(t.getCreatedAt(), t.getClosedAt()) <= t.getEstimatedMinutes())
                    .count();
            compliance = Math.round((double) onTime / closedWithEstimate.size() * 1000.0) / 10.0;
        }

        // Active breaches
        int activeBreaches = (int) allTickets.stream()
                .filter(t -> t.getStatus() == Status.OPEN || t.getStatus() == Status.IN_PROGRESS)
                .filter(t -> t.getEstimatedMinutes() != null)
                .filter(t -> ChronoUnit.MINUTES.between(t.getCreatedAt(), now) > t.getEstimatedMinutes())
                .count();

        // Avg resolution
        long avgResolution = (long) allTickets.stream()
                .filter(t -> (t.getStatus() == Status.CLOSED || t.getStatus() == Status.RESOLVED)
                        && t.getClosedAt() != null)
                .mapToLong(t -> ChronoUnit.MINUTES.between(t.getCreatedAt(), t.getClosedAt()))
                .average()
                .orElse(0.0);

        SlaSnapshot snapshot = new SlaSnapshot(today, compliance, activeBreaches, avgResolution);
        slaSnapshotRepository.save(snapshot);
        log.info("SLA snapshot saved for {}: compliance={}%, breaches={}", today, compliance, activeBreaches);
    }
}
