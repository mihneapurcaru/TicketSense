package com.gitlab.mihnea_purcaru1.service_ticketsense.controller;

import com.gitlab.mihnea_purcaru1.service_ticketsense.dto.UserDto;
import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.Role;
import com.gitlab.mihnea_purcaru1.service_ticketsense.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/it-support")
    public ResponseEntity<List<UserDto>> getItSupportUsers() {
        List<UserDto> users = userRepository.findByRole(Role.IT_SUPPORT_MEMBER).stream()
                .map(user -> {
                    UserDto dto = new UserDto();
                    dto.setId(user.getId());
                    dto.setFirstName(user.getFirstName());
                    dto.setLastName(user.getLastName());
                    dto.setUsername(user.getUsername());
                    dto.setEmail(user.getEmail());
                    dto.setRole(user.getRole().name());
                    return dto;
                })
                .toList();
        return ResponseEntity.ok(users);
    }
}
