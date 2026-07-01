package com.gitlab.mihnea_purcaru1.service_ticketsense.repository;

import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    java.util.List<User> findByRole(com.gitlab.mihnea_purcaru1.service_ticketsense.entity.Role role);
}
