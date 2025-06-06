package com.estuate.mpreplica.entity;

import com.estuate.mpreplica.enums.IdMeStatus;
import com.estuate.mpreplica.enums.LmsStatus;
import com.estuate.mpreplica.enums.SellerOverallStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "seller_profiles")
@Getter
@Setter
@NoArgsConstructor
public class SellerProfile extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "id_me_status", nullable = false, length=50)
    private IdMeStatus idMeStatus = IdMeStatus.PENDING;

    @Column(name = "id_me_external_id", length = 255)
    private String idMeExternalId;

    @Column(name = "id_me_verification_url", length = 512)
    private String idMeVerificationUrl;

    @Column(name = "id_me_verification_details_link", length = 512)
    private String idMeVerificationDetailsLink;

    @Enumerated(EnumType.STRING)
    @Column(name = "lms_status", nullable = false, length=50)
    private LmsStatus lmsStatus = LmsStatus.NOT_STARTED;

    @Column(name = "lms_external_id", length = 255)
    private String lmsExternalId;

    @Column(name = "lms_invitation_url", length = 512)
    private String lmsInvitationUrl;

    @Column(name = "lms_course_name", length = 255)
    private String lmsCourseName;

    @Column(name = "lms_completion_date_details", length = 100)
    private String lmsCompletionDateDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_status", nullable = false, length=50)
    private SellerOverallStatus overallStatus = SellerOverallStatus.PENDING_ID_ME;

    @Column(name = "status_reason", columnDefinition = "TEXT")
    private String statusReason;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "paypal_email", length = 255)
    private String payPalEmail;
}

