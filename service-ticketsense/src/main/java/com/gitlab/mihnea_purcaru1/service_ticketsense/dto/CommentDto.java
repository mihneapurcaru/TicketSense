package com.gitlab.mihnea_purcaru1.service_ticketsense.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDto {

    private Long id;

    private String message;

    private LocalDateTime createdAt;

    private Long ticketId;

    private Long userId;
    private String userFirstName;
    private String userLastName;
    private String userRole;
}
