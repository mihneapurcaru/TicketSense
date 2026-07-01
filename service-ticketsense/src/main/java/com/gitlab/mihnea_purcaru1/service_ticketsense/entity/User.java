package com.gitlab.mihnea_purcaru1.service_ticketsense.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(length = 50, nullable = false, unique = true)
    private String username;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Role role = Role.NORMAL_USER;

    @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;
}
