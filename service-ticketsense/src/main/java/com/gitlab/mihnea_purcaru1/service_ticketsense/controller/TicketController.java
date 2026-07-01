package com.gitlab.mihnea_purcaru1.service_ticketsense.controller;

import com.gitlab.mihnea_purcaru1.service_ticketsense.dto.StatisticsDto;
import com.gitlab.mihnea_purcaru1.service_ticketsense.dto.TicketDto;
import com.gitlab.mihnea_purcaru1.service_ticketsense.service.TicketService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class TicketController {
    private final TicketService ticketService;

    @PostMapping("/ticket")
    public ResponseEntity<TicketDto> createTicket(@RequestBody TicketDto ticketDto) {
        TicketDto savedTicket = ticketService.createTicket(ticketDto);
        return new ResponseEntity<>(savedTicket, HttpStatus.CREATED);
    }

    @GetMapping("/tickets")
    public ResponseEntity<List<TicketDto>> getTicketsByQueue(@RequestParam Long queueId) {
        List<TicketDto> tickets = ticketService.getTicketsByQueue(queueId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/tickets/my")
    public ResponseEntity<List<TicketDto>> getMyTickets(@RequestParam Long reporterId) {
        return ResponseEntity.ok(ticketService.getTicketsByReporter(reporterId));
    }

    @GetMapping("/ticket/{id}")
    public ResponseEntity<TicketDto> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @PutMapping("/ticket/{id}")
    public ResponseEntity<TicketDto> updateTicket(@PathVariable Long id, @RequestBody TicketDto ticketDto) {
        return ResponseEntity.ok(ticketService.updateTicket(id, ticketDto));
    }

    @GetMapping("/statistics/resolution-time")
    public ResponseEntity<StatisticsDto> getAverageResolutionTime() {
        return ResponseEntity.ok(ticketService.getAverageResolutionTime());
    }

    @PostMapping("/ticket/{id}/summarize")
    public ResponseEntity<TicketDto> summarizeTicket(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.summarizeTicket(id));
    }
}
