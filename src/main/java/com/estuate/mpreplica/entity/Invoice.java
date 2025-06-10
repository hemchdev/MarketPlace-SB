package com.estuate.mpreplica.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Represents a generated invoice document, linked to a completed payout.
 * It stores metadata about the invoice, including a reference to its storage location.
 */
@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
public class Invoice extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payout_id", nullable = false, unique = true)
    private Payout payout;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 100)
    private String invoiceNumber;

    /**
     * The path to the generated PDF file. This could be a local file path,
     * a cloud storage URL (e.g., S3), or an identifier for a document management system.
     */
    @Column(name = "pdf_storage_path", nullable = false, length = 1024)
    private String pdfStoragePath;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt = LocalDateTime.now();
}
