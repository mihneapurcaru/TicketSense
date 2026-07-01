package com.gitlab.mihnea_purcaru1.service_ticketsense.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketLogDto {

    private Long id;

    private TicketDto ticket;

    private UserDto user;

    private String message;

    private LocalDateTime createdAt;
}
