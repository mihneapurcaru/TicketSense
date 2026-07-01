package com.gitlab.mihnea_purcaru1.service_ticketsense.service;

import com.gitlab.mihnea_purcaru1.service_ticketsense.dto.StatisticsDto;
import com.gitlab.mihnea_purcaru1.service_ticketsense.dto.TopPerformerDto;
import com.gitlab.mihnea_purcaru1.service_ticketsense.dto.WorkloadDto;
import com.gitlab.mihnea_purcaru1.service_ticketsense.dto.TicketDto;
import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.Priority;
import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.Queue;
import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.Status;
import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.Ticket;
import com.gitlab.mihnea_purcaru1.service_ticketsense.exception.ResourceNotFoundException;
import com.gitlab.mihnea_purcaru1.service_ticketsense.mapper.TicketMapper;
import com.gitlab.mihnea_purcaru1.service_ticketsense.repository.QueueRepository;
import com.gitlab.mihnea_purcaru1.service_ticketsense.repository.TicketRepository;
import com.gitlab.mihnea_purcaru1.service_ticketsense.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final NlpClassifierService nlpClassifierService;
    private final AiSummarizationService aiSummarizationService;
    private final QueueRepository queueRepository;
    private final UserRepository userRepository;

    @Transactional
    public TicketDto createTicket(TicketDto ticketDto) {
        Ticket ticket = ticketMapper.mapToTicket(ticketDto);
        Ticket savedTicket = ticketRepository.save(ticket);

        NlpClassifierService.ClassifyResult result = nlpClassifierService.classify(
                ticketDto.getSummary(),
                ticketDto.getDescription()
        );

        Queue queue = queueRepository.findByName(result.getQueue())
                .orElseGet(() -> queueRepository.findByName("General").orElse(null));

        savedTicket.setQueue(queue);
        savedTicket = ticketRepository.save(savedTicket);

        return ticketMapper.mapToDto(savedTicket);
    }

    @Transactional(readOnly = true)
    public List<TicketDto> getTicketsByQueue(Long queueId) {
        return ticketRepository.findByQueueIdOrderByCreatedAtDesc(queueId)
                .stream()
                .map(ticketMapper::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketDto> getTicketsByReporter(Long reporterId) {
        return ticketRepository.findByReporterIdOrderByCreatedAtDesc(reporterId)
                .stream()
                .map(ticketMapper::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketDto getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));
        return ticketMapper.mapToDto(ticket);
    }

    @Transactional
    public TicketDto updateTicket(Long id, TicketDto dto) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));

        if (dto.getPriority() != null) {
            ticket.setPriority(Priority.valueOf(dto.getPriority()));
        }
        if (dto.getStatus() != null) {
            Status newStatus = Status.valueOf(dto.getStatus());
            if (newStatus == Status.CLOSED && ticket.getStatus() != Status.CLOSED) {
                ticket.setClosedAt(LocalDateTime.now());
            }
            ticket.setStatus(newStatus);
        }
        if (dto.getAssignedToId() != null) {
            userRepository.findById(dto.getAssignedToId()).ifPresent(assignee -> {
                ticket.setAssigned_to(assignee);

                if (assignee.getTeam() != null) {
                    String teamName = assignee.getTeam().getTeamName();
                    queueRepository.findByName(teamName).ifPresent(ticket::setQueue);
                }
            });
        }
        if (dto.getEstimatedMinutes() != null) {
            ticket.setEstimatedMinutes(dto.getEstimatedMinutes());
        }
        if (dto.getResolutionNote() != null) {
            ticket.setResolutionNote(dto.getResolutionNote());
        }

        return ticketMapper.mapToDto(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketDto summarizeTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));

        String summary = aiSummarizationService.summarize(ticket.getSummary(), ticket.getDescription());
        ticket.setAiSummary(summary);
        return ticketMapper.mapToDto(ticketRepository.save(ticket));
    }

    @Transactional(readOnly = true)
    public StatisticsDto getAverageResolutionTime() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Ticket> closedTickets = ticketRepository.findClosedTicketsInLast30Days(thirtyDaysAgo);
        Long criticalTickets = ticketRepository.countCriticalPriorityTicketsInLast30Days(thirtyDaysAgo);
        List<Long> volumeTrend = buildVolumeTrend(thirtyDaysAgo);
        List<Long> resolvedTrend = buildResolvedTrend(thirtyDaysAgo);
        long openTickets = ticketRepository.countByStatus(Status.OPEN);
        long inProgressTickets = ticketRepository.countByStatus(Status.IN_PROGRESS);
        long closedOrResolvedTickets = ticketRepository.countByStatusIn(List.of(Status.CLOSED, Status.RESOLVED));
        List<TopPerformerDto> topPerformers = ticketRepository.findTopPerformersInLast30Days(thirtyDaysAgo)
                .stream()
                .map(row -> new TopPerformerDto(
                        (String) row[0],
                        (String) row[1],
                        row[2] != null ? (String) row[2] : "General",
                        ((Number) row[3]).longValue()
                ))
                .toList();
        List<WorkloadDto> workload = ticketRepository.countOpenTicketsPerQueue()
                .stream()
                .map(row -> new WorkloadDto((String) row[0], ((Number) row[1]).longValue()))
                .toList();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");
        LocalDate startDateObj = LocalDate.now().minusDays(29);
        String startDate = startDateObj.format(formatter);
        String startDateISO = startDateObj.toString();
        String endDate = LocalDate.now().format(formatter);

        if (closedTickets.isEmpty()) {
            return new StatisticsDto(0.0, 0L, criticalTickets, volumeTrend, resolvedTrend, startDate, endDate, startDateISO, openTickets, inProgressTickets, closedOrResolvedTickets, topPerformers, workload);
        }

        double totalMinutes = closedTickets.stream()
                .mapToDouble(ticket -> java.time.temporal.ChronoUnit.MINUTES.between(ticket.getCreatedAt(), ticket.getClosedAt()))
                .sum();

        double averageHours = (totalMinutes / closedTickets.size()) / 60.0;

        return new StatisticsDto(averageHours, (long) closedTickets.size(), criticalTickets, volumeTrend, resolvedTrend, startDate, endDate, startDateISO, openTickets, inProgressTickets, closedOrResolvedTickets, topPerformers, workload);
    }

    private List<Long> buildVolumeTrend(LocalDateTime thirtyDaysAgo) {
        List<Object[]> rows = ticketRepository.countTicketsPerDayInLast30Days(thirtyDaysAgo);

        Map<LocalDate, Long> countByDay = new HashMap<>();
        for (Object[] row : rows) {
            LocalDate day;
            if (row[0] instanceof LocalDate) {
                day = (LocalDate) row[0];
            } else {
                day = ((java.sql.Date) row[0]).toLocalDate();
            }
            Long count = ((Number) row[1]).longValue();
            countByDay.put(day, count);
        }

        List<Long> trend = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            trend.add(countByDay.getOrDefault(day, 0L));
        }
        return trend;
    }

    private List<Long> buildResolvedTrend(LocalDateTime thirtyDaysAgo) {
        List<Object[]> rows = ticketRepository.countResolvedTicketsPerDayInLast30Days(thirtyDaysAgo);

        Map<LocalDate, Long> countByDay = new HashMap<>();
        for (Object[] row : rows) {
            LocalDate day;
            if (row[0] instanceof LocalDate) {
                day = (LocalDate) row[0];
            } else {
                day = ((java.sql.Date) row[0]).toLocalDate();
            }
            Long count = ((Number) row[1]).longValue();
            countByDay.put(day, count);
        }

        List<Long> trend = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            trend.add(countByDay.getOrDefault(day, 0L));
        }
        return trend;
    }
}
