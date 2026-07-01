package com.gitlab.mihnea_purcaru1.service_ticketsense.repository;

import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.Status;
import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByQueueIdOrderByCreatedAtDesc(Long queueId);
    List<Ticket> findByReporterIdOrderByCreatedAtDesc(Long reporterId);

    @Query("SELECT t FROM Ticket t WHERE t.status = 'CLOSED' AND t.closedAt IS NOT NULL AND t.closedAt >= :thirtyDaysAgo")
    List<Ticket> findClosedTicketsInLast30Days(LocalDateTime thirtyDaysAgo);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.priority = 'CRITICAL' AND t.createdAt >= :thirtyDaysAgo")
    Long countCriticalPriorityTicketsInLast30Days(LocalDateTime thirtyDaysAgo);

    @Query(value = "SELECT DATE(created_at) as day, COUNT(*) as count FROM tickets WHERE created_at >= :thirtyDaysAgo GROUP BY DATE(created_at) ORDER BY DATE(created_at)", nativeQuery = true)
    List<Object[]> countTicketsPerDayInLast30Days(LocalDateTime thirtyDaysAgo);

    @Query(value = "SELECT DATE(closed_at) as day, COUNT(*) as count FROM tickets WHERE status = 'CLOSED' AND closed_at IS NOT NULL AND closed_at >= :thirtyDaysAgo GROUP BY DATE(closed_at) ORDER BY DATE(closed_at)", nativeQuery = true)
    List<Object[]> countResolvedTicketsPerDayInLast30Days(LocalDateTime thirtyDaysAgo);

    long countByStatus(Status status);

    long countByStatusIn(List<Status> statuses);

    @Query(value = """
        SELECT u.first_name, u.last_name, tm.team_name, COUNT(t.id) as resolved_count
        FROM tickets t
        JOIN users u ON t.assigned_to_id = u.id
        LEFT JOIN teams tm ON u.team_id = tm.id
        WHERE t.status = 'CLOSED' AND t.closed_at IS NOT NULL AND t.closed_at >= :thirtyDaysAgo
        GROUP BY u.id, u.first_name, u.last_name, tm.team_name
        ORDER BY resolved_count DESC
        LIMIT 3
        """, nativeQuery = true)
    List<Object[]> findTopPerformersInLast30Days(LocalDateTime thirtyDaysAgo);

    @Query(value = """
        SELECT q.name, COUNT(t.id) as open_count
        FROM queues q
        LEFT JOIN tickets t ON t.queue_id = q.id AND t.status = 'OPEN'
        GROUP BY q.id, q.name
        ORDER BY q.display_order
        """, nativeQuery = true)
    List<Object[]> countOpenTicketsPerQueue();
}
