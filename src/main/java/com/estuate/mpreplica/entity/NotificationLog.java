package com.estuate.mpreplica.entity;

import com.estuate.mpreplica.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Entity
@Table(name = "notification_logs")
@Getter
@Setter
@NoArgsConstructor
public class NotificationLog extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_profile_id")
    private SellerProfile sellerProfile; // Assuming SellerProfile class exists

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Assuming User class exists

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 60)
    private NotificationType notificationType;

    @Column(columnDefinition = "TEXT")
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "external_tracking_id")
    private String externalTrackingId;

    // Constructor
    public NotificationLog(String recipientEmail, NotificationType notificationType, String subject, String message, String status) {
        this.recipientEmail = recipientEmail;
        this.notificationType = notificationType;
        this.subject = subject;
        this.message = message;
        this.status = status;
        this.sentAt = LocalDateTime.now(); // Ensure sentAt is initialized
    }
}