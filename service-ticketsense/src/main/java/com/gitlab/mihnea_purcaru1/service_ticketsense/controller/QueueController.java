package com.gitlab.mihnea_purcaru1.service_ticketsense.controller;

import com.gitlab.mihnea_purcaru1.service_ticketsense.dto.QueueDto;
import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.Queue;
import com.gitlab.mihnea_purcaru1.service_ticketsense.repository.QueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QueueController {

    private final QueueRepository queueRepository;

    @GetMapping("/queues")
    public ResponseEntity<List<QueueDto>> getAllQueues() {
        List<QueueDto> queues = queueRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(queue -> new QueueDto(
                        queue.getId(),
                        queue.getName(),
                        queue.getIcon(),
                        queue.getDescription()
                ))
                .toList();
        return ResponseEntity.ok(queues);
    }
}
