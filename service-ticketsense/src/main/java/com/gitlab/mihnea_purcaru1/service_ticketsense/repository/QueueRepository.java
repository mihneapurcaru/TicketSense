package com.gitlab.mihnea_purcaru1.service_ticketsense.repository;

import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.Queue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueueRepository extends JpaRepository<Queue, Long> {
    java.util.Optional<Queue> findByName(String name);
    java.util.List<Queue> findAllByOrderByDisplayOrderAsc();
}
