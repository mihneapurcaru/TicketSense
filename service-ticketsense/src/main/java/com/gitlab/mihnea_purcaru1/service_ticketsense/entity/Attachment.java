package com.gitlab.mihnea_purcaru1.service_ticketsense.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
@Getter
@Setter
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false, unique = true)
    private String filePath;

    @Column(name = "file_type", length = 50, nullable = false)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt = LocalDateTime.now();
}
