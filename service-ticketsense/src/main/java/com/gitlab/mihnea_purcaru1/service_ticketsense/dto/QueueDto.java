package com.gitlab.mihnea_purcaru1.service_ticketsense.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueueDto {

    private Long id;

    private String name;

    private String icon;

    private String description;
}
