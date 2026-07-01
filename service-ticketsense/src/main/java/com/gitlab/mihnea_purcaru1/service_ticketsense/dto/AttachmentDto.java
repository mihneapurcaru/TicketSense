package com.gitlab.mihnea_purcaru1.service_ticketsense.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttachmentDto {

    private Long id;

    private Long ticketId;

    private String fileName;

    private String fileType;

    private Long fileSize;

    private LocalDateTime uploadedAt;
}
