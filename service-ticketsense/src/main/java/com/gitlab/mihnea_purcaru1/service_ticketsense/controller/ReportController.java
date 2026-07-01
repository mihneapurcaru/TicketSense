package com.gitlab.mihnea_purcaru1.service_ticketsense.controller;

import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.Status;
import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.Ticket;
import com.gitlab.mihnea_purcaru1.service_ticketsense.repository.QueueRepository;
import com.gitlab.mihnea_purcaru1.service_ticketsense.repository.TicketRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final TicketRepository ticketRepository;
    private final QueueRepository queueRepository;

    @GetMapping("/teams")
    public ResponseEntity<List<TeamReportDto>> getTeamReports() {
        List<TeamReportDto> reports = queueRepository.findAllByOrderByDisplayOrderAsc().stream()
                .filter(q -> !q.getName().equalsIgnoreCase("General"))
                .map(queue -> {
                    List<Ticket> tickets = ticketRepository.findByQueueIdOrderByCreatedAtDesc(queue.getId());
                    return buildReport(queue.getName(), queue.getIcon(), tickets);
                })
                .toList();
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/teams/{queueName}/pdf")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> downloadTeamPdf(@PathVariable String queueName) {
        var queue = queueRepository.findByName(queueName).orElse(null);
        if (queue == null) return ResponseEntity.notFound().build();

        List<Ticket> tickets = ticketRepository.findByQueueIdOrderByCreatedAtDesc(queue.getId());
        TeamReportDto stats = buildReport(queue.getName(), queue.getIcon(), tickets);

        byte[] pdf = generatePdf(stats, tickets);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + queueName + "-report.pdf\"")
                .body(pdf);
    }

    private TeamReportDto buildReport(String name, String icon, List<Ticket> tickets) {
        long solved = tickets.stream()
                .filter(t -> t.getStatus() == Status.CLOSED || t.getStatus() == Status.RESOLVED)
                .count();

        List<Ticket> closedWithEstimate = tickets.stream()
                .filter(t -> (t.getStatus() == Status.CLOSED || t.getStatus() == Status.RESOLVED)
                        && t.getEstimatedMinutes() != null && t.getClosedAt() != null)
                .toList();

        double slaSuccess = 0;
        if (!closedWithEstimate.isEmpty()) {
            long onTime = closedWithEstimate.stream()
                    .filter(t -> ChronoUnit.MINUTES.between(t.getCreatedAt(), t.getClosedAt()) <= t.getEstimatedMinutes())
                    .count();
            slaSuccess = Math.round((double) onTime / closedWithEstimate.size() * 1000.0) / 10.0;
        }

        double avgMinutes = tickets.stream()
                .filter(t -> (t.getStatus() == Status.CLOSED || t.getStatus() == Status.RESOLVED)
                        && t.getClosedAt() != null)
                .mapToLong(t -> ChronoUnit.MINUTES.between(t.getCreatedAt(), t.getClosedAt()))
                .average()
                .orElse(0);

        TeamReportDto dto = new TeamReportDto();
        dto.setQueueName(name);
        dto.setIcon(icon);
        dto.setSolved((int) solved);
        dto.setSlaSuccessPercentage(slaSuccess);
        dto.setAvgResolutionMinutes((long) avgMinutes);
        return dto;
    }

    private byte[] generatePdf(TeamReportDto stats, List<Ticket> tickets) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, new Color(99, 102, 241));
            Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
            Font bodyFont = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(46, 48, 64));
            Font subFont = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(88, 90, 104));

            Paragraph title = new Paragraph(stats.getQueueName() + " Team Report", titleFont);
            title.setSpacingAfter(4);
            doc.add(title);

            Paragraph subtitle = new Paragraph(
                    "Generated on " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                    subFont);
            subtitle.setSpacingAfter(20);
            doc.add(subtitle);

            PdfPTable statsTable = new PdfPTable(3);
            statsTable.setWidthPercentage(100);
            statsTable.setSpacingAfter(24);

            addStatCell(statsTable, "Tickets Solved", String.valueOf(stats.getSolved()), headerFont, bodyFont);
            addStatCell(statsTable, "SLA Success", stats.getSlaSuccessPercentage() + "%", headerFont, bodyFont);
            addStatCell(statsTable, "Avg. Resolution", formatMinutes(stats.getAvgResolutionMinutes()), headerFont, bodyFont);

            doc.add(statsTable);

            Paragraph tableTitle = new Paragraph("Ticket Details", new Font(Font.HELVETICA, 13, Font.BOLD, new Color(46, 48, 64)));
            tableTitle.setSpacingAfter(10);
            doc.add(tableTitle);

            PdfPTable table = new PdfPTable(new float[]{1.2f, 3f, 1.5f, 1.5f, 1.8f});
            table.setWidthPercentage(100);

            Color headerBg = new Color(99, 102, 241);
            for (String col : new String[]{"ID", "Summary", "Priority", "Status", "Assigned To"}) {
                PdfPCell cell = new PdfPCell(new Phrase(col, headerFont));
                cell.setBackgroundColor(headerBg);
                cell.setPadding(8);
                cell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                table.addCell(cell);
            }

            Color rowAlt = new Color(240, 242, 248);
            int row = 0;
            for (Ticket t : tickets) {
                Color bg = (row++ % 2 == 0) ? Color.WHITE : rowAlt;
                addBodyCell(table, "TS-" + t.getId(), bodyFont, bg);
                addBodyCell(table, t.getSummary(), bodyFont, bg);
                addBodyCell(table, t.getPriority() != null ? t.getPriority().name() : "-", bodyFont, bg);
                addBodyCell(table, t.getStatus().name().replace("_", " "), bodyFont, bg);
                String assignee = t.getAssigned_to() != null
                        ? t.getAssigned_to().getFirstName() + " " + t.getAssigned_to().getLastName()
                        : "Unassigned";
                addBodyCell(table, assignee, bodyFont, bg);
            }

            doc.add(table);

            Paragraph footer = new Paragraph("\nTicketSense ITSM - Confidential",
                    new Font(Font.HELVETICA, 8, Font.ITALIC, new Color(138, 140, 154)));
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    private void addStatCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(new Color(99, 102, 241));
        cell.setPadding(12);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.addElement(new Phrase(label, labelFont));
        Font bigValue = new Font(Font.HELVETICA, 18, Font.BOLD, Color.WHITE);
        cell.addElement(new Phrase(value, bigValue));
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text, Font font, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(7);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }

    private String formatMinutes(long minutes) {
        if (minutes == 0) return "-";
        long h = minutes / 60;
        long m = minutes % 60;
        if (h == 0) return m + "m";
        if (m == 0) return h + "h";
        return h + "h " + m + "m";
    }

    @Data
    public static class TeamReportDto {
        private String queueName;
        private String icon;
        private int solved;
        private double slaSuccessPercentage;
        private long avgResolutionMinutes;
    }
}
