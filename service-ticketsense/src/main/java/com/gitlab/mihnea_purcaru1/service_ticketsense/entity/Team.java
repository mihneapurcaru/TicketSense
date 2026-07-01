package com.gitlab.mihnea_purcaru1.service_ticketsense.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "teams")
@Getter
@Setter
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "team_name", length = 50, nullable = false, unique = true)
    private String teamName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "team")
    private List<User> members;
}
