package com.gitlab.mihnea_purcaru1.service_ticketsense.repository;

import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByTicketId(Long ticketId);
}
