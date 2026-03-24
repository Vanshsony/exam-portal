package com.examportal.service;

import com.examportal.entity.ExamAttempt;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    public byte[] generateResultPdf(ExamAttempt attempt) throws Exception {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        // Fonts
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, new BaseColor(33, 37, 41));
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, new BaseColor(13, 110, 253));
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.DARK_GRAY);
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font passFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, new BaseColor(25, 135, 84));
        Font failFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, new BaseColor(220, 53, 69));

        // Header bar
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        PdfPCell headerCell = new PdfPCell(new Phrase("EXAM PORTAL", titleFont));
        headerCell.setBackgroundColor(new BaseColor(13, 110, 253));
        headerCell.setPadding(15);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerCell.setPhrase(new Phrase("EXAM PORTAL", new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.WHITE)));
        headerTable.addCell(headerCell);
        document.add(headerTable);

        document.add(Chunk.NEWLINE);

        // Title
        Paragraph title = new Paragraph("RESULT CERTIFICATE", headerFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(Chunk.NEWLINE);

        // Student Info Table
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1, 2});

        addTableRow(infoTable, "Student Name", attempt.getStudent().getFullName(), boldFont, normalFont);
        addTableRow(infoTable, "Username", attempt.getStudent().getUsername(), boldFont, normalFont);
        addTableRow(infoTable, "Email", attempt.getStudent().getEmail(), boldFont, normalFont);
        addTableRow(infoTable, "Exam", attempt.getExam().getTitle(), boldFont, normalFont);
        addTableRow(infoTable, "Date", attempt.getSubmittedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")), boldFont, normalFont);
        document.add(infoTable);

        document.add(Chunk.NEWLINE);

        // Score Section
        PdfPTable scoreTable = new PdfPTable(3);
        scoreTable.setWidthPercentage(100);
        addScoreCell(scoreTable, "Score Obtained", String.valueOf(attempt.getScore()), new BaseColor(13, 110, 253));
        addScoreCell(scoreTable, "Total Marks", String.valueOf(attempt.getTotalMarks()), new BaseColor(108, 117, 125));
        addScoreCell(scoreTable, "Percentage", String.format("%.1f%%", (attempt.getScore() * 100.0 / attempt.getTotalMarks())), new BaseColor(32, 201, 151));
        document.add(scoreTable);

        document.add(Chunk.NEWLINE);

        // Pass/Fail
        Paragraph status = new Paragraph(attempt.isPassed() ? "✓  PASSED" : "✗  FAILED",
            attempt.isPassed() ? passFont : failFont);
        status.setAlignment(Element.ALIGN_CENTER);
        document.add(status);

        if (attempt.getTabSwitchCount() > 0) {
            document.add(Chunk.NEWLINE);
            Paragraph warning = new Paragraph("⚠ Note: " + attempt.getTabSwitchCount() + " tab switch(es) detected during exam.",
                new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, new BaseColor(255, 193, 7)));
            warning.setAlignment(Element.ALIGN_CENTER);
            document.add(warning);
        }

        // Footer
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);
        Paragraph footer = new Paragraph("This is a system-generated result. No signature required.",
            new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, BaseColor.GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
        return out.toByteArray();
    }

    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setPadding(8);
        labelCell.setBackgroundColor(new BaseColor(248, 249, 250));
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setPadding(8);
        table.addCell(valueCell);
    }

    private void addScoreCell(PdfPTable table, String label, String value, BaseColor color) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(15);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(color);

        Paragraph p = new Paragraph();
        p.add(new Chunk(value + "\n", new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.WHITE)));
        p.add(new Chunk(label, new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.WHITE)));
        p.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(p);
        table.addCell(cell);
    }
}
