package com.gitlab.mihnea_purcaru1.service_ticketsense.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsDto {
    private Double averageResolutionHours;
    private Long totalClosedTickets;
    private Long criticalPriorityTickets;
    private List<Long> volumeTrend;
    private List<Long> resolvedTrend;
    private String volumeTrendStartDate;
    private String volumeTrendEndDate;
    private String volumeTrendStartDateISO;
    private Long openTickets;
    private Long inProgressTickets;
    private Long closedOrResolvedTickets;
    private List<TopPerformerDto> topPerformers;
    private List<WorkloadDto> workload;
}
