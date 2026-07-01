package com.gitlab.mihnea_purcaru1.service_ticketsense.dto;

import lombok.Data;

@Data
public class UserDto {

    private Long id;

    private String firstName;

    private String lastName;

    private String username;

    private String email;

    private String role;

    private Boolean isActive;

    private TeamDto team;
}
