package com.gitlab.mihnea_purcaru1.service_ticketsense.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorDetails {
    private LocalDateTime timestamp;
    private String message;
    private String details;
}
