package com.gitlab.mihnea_purcaru1.service_ticketsense.controller;

import com.gitlab.mihnea_purcaru1.service_ticketsense.dto.AttachmentDto;
import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.Attachment;
import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.Ticket;
import com.gitlab.mihnea_purcaru1.service_ticketsense.exception.ResourceNotFoundException;
import com.gitlab.mihnea_purcaru1.service_ticketsense.repository.AttachmentRepository;
import com.gitlab.mihnea_purcaru1.service_ticketsense.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentRepository attachmentRepository;
    private final TicketRepository ticketRepository;

    @Value("${uploads.dir:/uploads}")
    private String uploadsDir;

    @GetMapping("/ticket/{ticketId}/attachments")
    public ResponseEntity<List<AttachmentDto>> getAttachments(@PathVariable Long ticketId) {
        List<AttachmentDto> attachments = attachmentRepository.findByTicketId(ticketId)
                .stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(attachments);
    }

    @PostMapping("/ticket/{ticketId}/attachments")
    public ResponseEntity<AttachmentDto> upload(
            @PathVariable Long ticketId,
            @RequestParam("file") MultipartFile file) throws IOException {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        Path ticketDir = Paths.get(uploadsDir, String.valueOf(ticketId));
        Files.createDirectories(ticketDir);

        String uniqueName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = ticketDir.resolve(uniqueName);
        Files.copy(file.getInputStream(), filePath);

        Attachment attachment = new Attachment();
        attachment.setTicket(ticket);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFilePath(filePath.toString());
        attachment.setFileType(file.getContentType());
        attachment.setFileSize(file.getSize());

        return ResponseEntity.ok(toDto(attachmentRepository.save(attachment)));
    }

    @GetMapping("/attachments/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) throws MalformedURLException {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + id));

        Path filePath = Paths.get(attachment.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            throw new ResourceNotFoundException("File not found on disk: " + attachment.getFileName());
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(resource);
    }

    @GetMapping("/attachments/{id}/preview")
    public ResponseEntity<Resource> preview(@PathVariable Long id) throws MalformedURLException {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + id));

        Path filePath = Paths.get(attachment.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            throw new ResourceNotFoundException("File not found on disk: " + attachment.getFileName());
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + attachment.getFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/attachments/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws IOException {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + id));

        Files.deleteIfExists(Paths.get(attachment.getFilePath()));
        attachmentRepository.delete(attachment);

        return ResponseEntity.noContent().build();
    }

    private AttachmentDto toDto(Attachment a) {
        AttachmentDto dto = new AttachmentDto();
        dto.setId(a.getId());
        dto.setTicketId(a.getTicket().getId());
        dto.setFileName(a.getFileName());
        dto.setFileType(a.getFileType());
        dto.setFileSize(a.getFileSize());
        dto.setUploadedAt(a.getUploadedAt());
        return dto;
    }
}
