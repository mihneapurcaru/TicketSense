package com.gitlab.mihnea_purcaru1.service_ticketsense.repository;

import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.SlaSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SlaSnapshotRepository extends JpaRepository<SlaSnapshot, Long> {
    List<SlaSnapshot> findTop30ByOrderBySnapshotDateAsc();
    Optional<SlaSnapshot> findBySnapshotDate(LocalDate date);
}
