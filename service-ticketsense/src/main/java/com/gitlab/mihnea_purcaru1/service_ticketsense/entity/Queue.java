package com.gitlab.mihnea_purcaru1.service_ticketsense.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "queues")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Queue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String name;

    @Column(length = 50)
    private String icon;

    @Column(length = 200)
    private String description;

    @Column(name = "display_order", columnDefinition = "INT DEFAULT 0")
    private Integer displayOrder = 0;

    @OneToMany(mappedBy = "queue", fetch = FetchType.LAZY)
    private List<Ticket> tickets;
}
