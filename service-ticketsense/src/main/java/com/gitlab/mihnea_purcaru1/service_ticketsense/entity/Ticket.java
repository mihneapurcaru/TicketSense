package com.gitlab.mihnea_purcaru1.service_ticketsense.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "tickets")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assigned_to;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Status status = Status.OPEN;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    @Column(name = "resolution_note", columnDefinition = "TEXT")
    private String resolutionNote;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "queue_id")
    private Queue queue;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    private List<Comment> comments;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    private List<Attachment> attachments;
}
