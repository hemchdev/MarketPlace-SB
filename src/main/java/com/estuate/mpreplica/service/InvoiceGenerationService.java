package com.estuate.mpreplica.service;

import com.estuate.mpreplica.entity.Invoice;
import com.estuate.mpreplica.entity.Payout;
import com.estuate.mpreplica.events.PayoutCompletedEvent;
import com.estuate.mpreplica.repository.InvoiceRepository;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

/**
 * Handles the generation of PDF invoices upon successful payout completion.
 * This service listens for PayoutCompletedEvent and runs asynchronously.
 */
@Service
public class InvoiceGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceGenerationService.class);

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Value("${invoicing.storage.path:/tmp/invoices}")
    private String invoiceStoragePath;

    @Async
    @EventListener
    public void handlePayoutCompleted(PayoutCompletedEvent event) {
        Payout payout = event.getPayout();
        logger.info("Received PayoutCompletedEvent for Payout ID: {}. Generating invoice.", payout.getId());

        String invoiceNumber = String.format("INV-%s-%d",
                payout.getCompletedAt().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                payout.getId());

        String filePath = invoiceStoragePath + File.separator + invoiceNumber + ".pdf";

        try {
            // Ensure the directory exists
            Files.createDirectories(Paths.get(invoiceStoragePath));

            // Generate the PDF content
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            document.addTitle("Payout Invoice " + invoiceNumber);
            document.add(new Paragraph("Marketplace Payout Invoice"));
            document.add(new Paragraph("-------------------------------------"));
            document.add(new Paragraph("Invoice Number: " + invoiceNumber));
            document.add(new Paragraph("Payout Date: " + payout.getCompletedAt().toLocalDate()));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Seller: " + payout.getSellerProfile().getName()));
            document.add(new Paragraph("PSP Transaction ID: " + payout.getPspTransactionId()));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Amount Paid: " + payout.getAmount().toPlainString() + " USD"));
            document.add(new Paragraph("-------------------------------------"));
            document.close();

            // Create and save the Invoice entity record
            Invoice invoice = new Invoice();
            invoice.setPayout(payout);
            invoice.setInvoiceNumber(invoiceNumber);
            invoice.setPdfStoragePath(filePath);
            invoiceRepository.save(invoice);

            logger.info("Successfully generated invoice {} for Payout ID {} and saved to {}",
                    invoiceNumber, payout.getId(), filePath);

        } catch (Exception e) {
            logger.error("Failed to generate PDF invoice for Payout ID {}: {}", payout.getId(), e.getMessage(), e);
        }
    }
}
