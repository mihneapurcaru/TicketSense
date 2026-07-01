package com.gitlab.mihnea_purcaru1.service_ticketsense.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopPerformerDto {
    private String firstName;
    private String lastName;
    private String teamName;
    private Long resolvedCount;
}
