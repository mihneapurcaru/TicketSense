package com.gitlab.mihnea_purcaru1.service_ticketsense.controller;

import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.SlaSnapshot;
import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.Status;
import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.Ticket;
import com.gitlab.mihnea_purcaru1.service_ticketsense.repository.SlaSnapshotRepository;
import com.gitlab.mihnea_purcaru1.service_ticketsense.repository.TicketRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sla")
@RequiredArgsConstructor
public class SlaController {

    private final TicketRepository ticketRepository;
    private final SlaSnapshotRepository slaSnapshotRepository;

    private static final long NEAR_BREACH_MINUTES = 120;

    @GetMapping("/summary")
    @Transactional(readOnly = true)
    public ResponseEntity<SummaryDto> getSummary() {
        List<Ticket> allTickets = ticketRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        List<Ticket> openTickets = allTickets.stream()
                .filter(t -> t.getStatus() == Status.OPEN || t.getStatus() == Status.IN_PROGRESS)
                .toList();

        // Active breaches: open tickets past their estimated time
        long activeBreaches = openTickets.stream()
                .filter(t -> t.getEstimatedMinutes() != null)
                .filter(t -> ChronoUnit.MINUTES.between(t.getCreatedAt(), now) > t.getEstimatedMinutes())
                .count();

        // Near breach: open tickets that will breach within 2 hours
        long nearBreach = openTickets.stream()
                .filter(t -> t.getEstimatedMinutes() != null)
                .filter(t -> {
                    long elapsedMinutes = ChronoUnit.MINUTES.between(t.getCreatedAt(), now);
                    long remainingMinutes = t.getEstimatedMinutes() - elapsedMinutes;
                    return remainingMinutes > 0 && remainingMinutes <= NEAR_BREACH_MINUTES;
                })
                .count();

        // SLA Compliance: % of closed tickets resolved within estimated time
        List<Ticket> closedWithEstimate = allTickets.stream()
                .filter(t -> (t.getStatus() == Status.CLOSED || t.getStatus() == Status.RESOLVED)
                        && t.getEstimatedMinutes() != null && t.getClosedAt() != null)
                .toList();

        double compliance = 0.0;
        if (!closedWithEstimate.isEmpty()) {
            long onTime = closedWithEstimate.stream()
                    .filter(t -> ChronoUnit.MINUTES.between(t.getCreatedAt(), t.getClosedAt()) <= t.getEstimatedMinutes())
                    .count();
            compliance = (double) onTime / closedWithEstimate.size() * 100;
        }

        // Avg resolution time (minutes) for closed tickets
        double avgResolutionMinutes = allTickets.stream()
                .filter(t -> (t.getStatus() == Status.CLOSED || t.getStatus() == Status.RESOLVED)
                        && t.getClosedAt() != null)
                .mapToLong(t -> ChronoUnit.MINUTES.between(t.getCreatedAt(), t.getClosedAt()))
                .average()
                .orElse(0.0);

        SummaryDto dto = new SummaryDto();
        dto.setCompliancePercentage(Math.round(compliance * 10.0) / 10.0);
        dto.setActiveBreaches((int) activeBreaches);
        dto.setNearBreachCount((int) nearBreach);
        dto.setAvgResolutionMinutes((long) avgResolutionMinutes);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/watchlist")
    @Transactional(readOnly = true)
    public ResponseEntity<List<WatchlistItemDto>> getWatchlist() {
        LocalDateTime now = LocalDateTime.now();

        List<WatchlistItemDto> watchlist = ticketRepository.findAll().stream()
                .filter(t -> t.getStatus() == Status.OPEN || t.getStatus() == Status.IN_PROGRESS)
                .filter(t -> t.getEstimatedMinutes() != null)
                .map(t -> {
                    long elapsedMinutes = ChronoUnit.MINUTES.between(t.getCreatedAt(), now);
                    long remainingMinutes = t.getEstimatedMinutes() - elapsedMinutes;
                    WatchlistItemDto item = new WatchlistItemDto();
                    item.setId(t.getId());
                    item.setPriority(t.getPriority() != null ? t.getPriority().name() : "MEDIUM");
                    item.setQueueName(t.getQueue() != null ? t.getQueue().getName() : "General");
                    item.setStatus(t.getStatus().name());
                    item.setRemainingMinutes(remainingMinutes);
                    return item;
                })
                .filter(item -> item.getRemainingMinutes() <= NEAR_BREACH_MINUTES)
                .sorted((a, b) -> Long.compare(a.getRemainingMinutes(), b.getRemainingMinutes()))
                .limit(10)
                .toList();

        return ResponseEntity.ok(watchlist);
    }

    @GetMapping("/trend")
    @Transactional(readOnly = true)
    public ResponseEntity<List<TrendPointDto>> getTrend() {
        List<TrendPointDto> trend = slaSnapshotRepository.findTop30ByOrderBySnapshotDateAsc()
                .stream()
                .map(s -> {
                    TrendPointDto dto = new TrendPointDto();
                    dto.setDate(s.getSnapshotDate().toString());
                    dto.setCompliancePercentage(s.getCompliancePercentage());
                    dto.setActiveBreaches(s.getActiveBreaches());
                    return dto;
                })
                .toList();
        return ResponseEntity.ok(trend);
    }

    @GetMapping("/breaches-by-queue")
    @Transactional(readOnly = true)
    public ResponseEntity<List<BreachByQueueDto>> getBreachesByQueue() {
        LocalDateTime now = LocalDateTime.now();

        Map<String, Long> breachCounts = ticketRepository.findAll().stream()
                .filter(t -> t.getEstimatedMinutes() != null
                        && t.getQueue() != null
                        && (t.getStatus() == Status.OPEN || t.getStatus() == Status.IN_PROGRESS)
                        && ChronoUnit.MINUTES.between(t.getCreatedAt(), now) > t.getEstimatedMinutes())
                .collect(Collectors.groupingBy(
                        t -> t.getQueue().getName(),
                        Collectors.counting()
                ));

        // Also count historical breaches (closed tickets that exceeded estimate)
        Map<String, Long> historicalBreaches = ticketRepository.findAll().stream()
                .filter(t -> t.getEstimatedMinutes() != null
                        && t.getQueue() != null
                        && t.getClosedAt() != null
                        && ChronoUnit.MINUTES.between(t.getCreatedAt(), t.getClosedAt()) > t.getEstimatedMinutes())
                .collect(Collectors.groupingBy(
                        t -> t.getQueue().getName(),
                        Collectors.counting()
                ));

        // Merge counts
        historicalBreaches.forEach((queue, count) ->
                breachCounts.merge(queue, count, Long::sum)
        );

        long maxBreaches = breachCounts.values().stream().mapToLong(Long::longValue).max().orElse(1L);

        List<BreachByQueueDto> result = breachCounts.entrySet().stream()
                .map(e -> {
                    BreachByQueueDto dto = new BreachByQueueDto();
                    dto.setQueueName(e.getKey());
                    dto.setBreachCount(e.getValue().intValue());
                    dto.setPercentage((int) Math.round((double) e.getValue() / maxBreaches * 100));
                    return dto;
                })
                .sorted((a, b) -> Integer.compare(b.getBreachCount(), a.getBreachCount()))
                .toList();

        return ResponseEntity.ok(result);
    }

    @Data
    public static class SummaryDto {
        private double compliancePercentage;
        private int activeBreaches;
        private int nearBreachCount;
        private long avgResolutionMinutes;
    }

    @Data
    public static class WatchlistItemDto {
        private Long id;
        private String priority;
        private String queueName;
        private String status;
        private long remainingMinutes;
    }

    @Data
    public static class TrendPointDto {
        private String date;
        private double compliancePercentage;
        private int activeBreaches;
    }

    @Data
    public static class BreachByQueueDto {
        private String queueName;
        private int breachCount;
        private int percentage;
    }
}
