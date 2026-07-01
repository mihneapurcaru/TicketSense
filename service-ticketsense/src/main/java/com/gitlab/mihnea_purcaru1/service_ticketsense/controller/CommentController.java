package com.gitlab.mihnea_purcaru1.service_ticketsense.controller;

import com.gitlab.mihnea_purcaru1.service_ticketsense.dto.CommentDto;
import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.Comment;
import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.Ticket;
import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.User;
import com.gitlab.mihnea_purcaru1.service_ticketsense.exception.ResourceNotFoundException;
import com.gitlab.mihnea_purcaru1.service_ticketsense.repository.CommentRepository;
import com.gitlab.mihnea_purcaru1.service_ticketsense.repository.TicketRepository;
import com.gitlab.mihnea_purcaru1.service_ticketsense.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @GetMapping("/ticket/{ticketId}/comments")
    @Transactional(readOnly = true)
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable Long ticketId) {
        List<CommentDto> comments = commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/ticket/{ticketId}/comments")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long ticketId,
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Comment comment = new Comment();
        comment.setMessage(request.getMessage());
        comment.setTicket(ticket);
        comment.setUser(user);

        return ResponseEntity.ok(toDto(commentRepository.save(comment)));
    }

    private CommentDto toDto(Comment c) {
        CommentDto dto = new CommentDto();
        dto.setId(c.getId());
        dto.setMessage(c.getMessage());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setTicketId(c.getTicket().getId());
        dto.setUserId(c.getUser().getId());
        dto.setUserFirstName(c.getUser().getFirstName());
        dto.setUserLastName(c.getUser().getLastName());
        dto.setUserRole(c.getUser().getRole().name());
        return dto;
    }

    @Data
    public static class CommentRequest {
        private String message;
    }
}
