package com.gitlab.mihnea_purcaru1.service_ticketsense.dto;

import lombok.Data;

import java.util.List;

@Data
public class TeamDto {

    private Long id;

    private String teamName;

    private String description;

    private List<UserDto> members;
}
