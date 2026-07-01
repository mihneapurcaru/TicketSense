package com.gitlab.mihnea_purcaru1.service_ticketsense.mapper;

import com.gitlab.mihnea_purcaru1.service_ticketsense.dto.TicketDto;
import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TicketMapper {

    @Mapping(target = "reporterId", source = "reporter.id")
    @Mapping(target = "reporterFirstName", source = "reporter.firstName")
    @Mapping(target = "reporterLastName", source = "reporter.lastName")
    @Mapping(target = "assignedToId", source = "assigned_to.id")
    @Mapping(target = "assignedToFirstName", source = "assigned_to.firstName")
    @Mapping(target = "assignedToLastName", source = "assigned_to.lastName")
    @Mapping(target = "teamId", source = "team.id")
    @Mapping(target = "queueId", source = "queue.id")
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    TicketDto mapToDto(Ticket ticket);

    @Mapping(target = "reporter", source = "reporterId", qualifiedByName = "idToUser")
    @Mapping(target = "assigned_to", source = "assignedToId", qualifiedByName = "idToUser")
    @Mapping(target = "team", source = "teamId", qualifiedByName = "idToTeam")
    @Mapping(target = "queue", source = "queueId", qualifiedByName = "idToQueue")
    @Mapping(target = "status", source = "status", defaultValue = "OPEN")
    Ticket mapToTicket(TicketDto ticketDto);

    @Named("idToUser")
    default User idToUser(Long id) {
        if (id == null) {
            return null;
        }
        User user = new User();
        user.setId(id);
        return user;
    }

    @Named("idToTeam")
    default Team idToTeam(Long id) {
        if (id == null) {
            return null;
        }
        Team team = new Team();
        team.setId(id);
        return team;
    }

    @Named("idToQueue")
    default Queue idToQueue(Long id) {
        if (id == null) {
            return null;
        }
        Queue queue = new Queue();
        queue.setId(id);
        return queue;
    }
}
