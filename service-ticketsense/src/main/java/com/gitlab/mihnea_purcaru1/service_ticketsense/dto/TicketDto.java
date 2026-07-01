package com.gitlab.mihnea_purcaru1.service_ticketsense.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketDto {

    private Long id;

    private String summary;

    private String description;

    private Long reporterId;
    private String reporterFirstName;
    private String reporterLastName;

    private Long assignedToId;
    private String assignedToFirstName;
    private String assignedToLastName;

    private Long teamId;

    private Long queueId;

    private String priority;

    private String status;

    private Integer estimatedMinutes;

    private String resolutionNote;

    private String aiSummary;

    private LocalDateTime createdAt;

    private LocalDateTime closedAt;

    private List<CommentDto> comments;

    private List<AttachmentDto> attachments;
}