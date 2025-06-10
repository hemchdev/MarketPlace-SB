package com.estuate.mpreplica.service;

import com.estuate.mpreplica.dto.*;
import com.estuate.mpreplica.entity.SellerProfile;
import com.estuate.mpreplica.entity.User;
import com.estuate.mpreplica.enums.IdMeStatus;
import com.estuate.mpreplica.enums.LmsStatus;
import com.estuate.mpreplica.enums.RoleName;
import com.estuate.mpreplica.enums.SellerOverallStatus;
import com.estuate.mpreplica.exception.ResourceNotFoundException;
import com.estuate.mpreplica.mapper.SellerProfileMapper;
import com.estuate.mpreplica.repository.SellerProfileRepository;
import com.estuate.mpreplica.repository.UserRepository;
import com.estuate.mpreplica.security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SellerProfileService {
    private static final Logger logger = LoggerFactory.getLogger(SellerProfileService.class);

    @Autowired private UserService userService;
    @Autowired private SellerProfileRepository sellerProfileRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SellerProfileMapper sellerProfileMapper;
    @Autowired private NotificationService notificationService;

    @Value("${marketplace.default.seller.rating:3.0}")
    private Double defaultSellerRating;

    private UserDetailsImpl getAuthenticatedUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return (UserDetailsImpl) authentication.getPrincipal();
        }
        return null;
    }

    private String getAuthenticatedUsername() {
        UserDetailsImpl userDetails = getAuthenticatedUserDetails();
        return (userDetails != null) ? userDetails.getUsername() : "SYSTEM";
    }

    @Transactional
    public SellerProfileDto createSellerWithProfile(CreateSellerRequestDto request) {
        UserResponseDto userResponseDto = userService.createUser(request.getUser(), Set.of(RoleName.SELLER));
        User user = userRepository.findById(userResponseDto.getId())
                .orElseThrow(() -> new RuntimeException("Newly created user not found with ID: " + userResponseDto.getId()));

        SellerProfile sellerProfile = new SellerProfile();
        sellerProfile.setUser(user);
        sellerProfile.setName(request.getName());
        sellerProfile.setContactPhone(request.getContactPhone());
        sellerProfile.setAddress(request.getAddress());

        double initialRating = (request.getRating() != null) ? request.getRating() : defaultSellerRating;
        sellerProfile.setCurrentRating(initialRating);

        // CORRECTED: Set payPalEmail from the request DTO
        sellerProfile.setPayPalEmail(request.getPayPalEmail());

        SellerProfile savedProfile = sellerProfileRepository.save(sellerProfile);
        logger.info("SellerProfile created for {}, ID {}, with initial rating {} and PayPal email {}",
                user.getUsername(), savedProfile.getId(), savedProfile.getCurrentRating(), savedProfile.getPayPalEmail());
        return sellerProfileMapper.toDto(savedProfile);
    }

    @Transactional
    public SellerProfileDto updateSellerDetails(Long sellerProfileId, SellerDetailsUpdateDto dto) {
        SellerProfile profile = getSellerProfileEntityById(sellerProfileId);
        String adminUsername = getAuthenticatedUsername();
        logger.info("Operator {} updating details for seller ID: {}", adminUsername, sellerProfileId);

        if (dto.getRating() != null) {
            profile.setCurrentRating(dto.getRating().doubleValue());
            logger.info("Seller ID {} currentRating updated to: {}", sellerProfileId, profile.getCurrentRating());
        }

        // CORRECTED: Update payPalEmail if it is provided in the DTO
        if (StringUtils.hasText(dto.getPayPalEmail())) {
            profile.setPayPalEmail(dto.getPayPalEmail());
            logger.info("Seller ID {} PayPal email updated to: {}", sellerProfileId, dto.getPayPalEmail());
        }

        SellerProfile updatedProfile = sellerProfileRepository.save(profile);
        return sellerProfileMapper.toDto(updatedProfile);
    }

    // ... other methods in SellerProfileService remain the same ...
    public SellerProfileDto getSellerProfileById(Long id) {
        return sellerProfileMapper.toDto(getSellerProfileEntityById(id));
    }

    public SellerProfile getSellerProfileEntityById(Long id) {
        return sellerProfileRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("SellerProfile", "id", id));
    }

    public List<SellerProfileDto> getAllSellerProfiles() {
        return sellerProfileRepository.findAll().stream()
                .map(sellerProfileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public SellerProfileDto updateSellerOverallStatus(Long sellerProfileId, SellerStatusUpdateDto dto) {
        SellerProfile profile = getSellerProfileEntityById(sellerProfileId);
        String adminUsername = getAuthenticatedUsername();

        logger.info("Administrator {} is updating overall status for SellerProfile ID {} from {} to {}. Reason: {}",
                adminUsername, sellerProfileId, profile.getOverallStatus(), dto.getOverallStatus(), dto.getReason());

        profile.setOverallStatus(dto.getOverallStatus());
        profile.setStatusReason(dto.getReason());

        if (dto.getOverallStatus() == SellerOverallStatus.ACTIVE) {
            String manualOverrideReasonPart = " Manually set to %s by admin " + adminUsername + ".";
            String currentReason = StringUtils.hasText(profile.getStatusReason()) ? profile.getStatusReason() : "";

            if (profile.getIdMeStatus() != IdMeStatus.APPROVED) {
                profile.setIdMeStatus(IdMeStatus.APPROVED);
                currentReason += String.format(manualOverrideReasonPart, "ID.me APPROVED");
            }
            if (profile.getLmsStatus() != LmsStatus.COMPLETED) {
                profile.setLmsStatus(LmsStatus.COMPLETED);
                currentReason += String.format(manualOverrideReasonPart, "LMS COMPLETED");
            }
            profile.setStatusReason(currentReason.trim());
        }

        SellerProfile updatedProfile = sellerProfileRepository.save(profile);
        notificationService.sendSellerStatusManuallyUpdated(updatedProfile, adminUsername);
        return sellerProfileMapper.toDto(updatedProfile);
    }

    @Transactional
    public SellerProfileDto initiateIdMeVerification(Long sellerProfileId) {
        SellerProfile profile = getSellerProfileEntityById(sellerProfileId);

        if (profile.getIdMeStatus() == IdMeStatus.PENDING || profile.getIdMeStatus() == IdMeStatus.DECLINED) {
            profile.setIdMeStatus(IdMeStatus.VERIFICATION_INITIATED);
            String verificationUrl = "https://id.me/simulated-verification/" + UUID.randomUUID().toString();
            profile.setIdMeVerificationUrl(verificationUrl);
            profile.setOverallStatus(SellerOverallStatus.PENDING_ID_ME);

            SellerProfile updatedProfile = sellerProfileRepository.save(profile);
            notificationService.sendIdMeActionRequired(updatedProfile, verificationUrl);
            logger.info("ID.me verification initiated for SellerProfile ID {}. URL: {}", updatedProfile.getId(), verificationUrl);
            return sellerProfileMapper.toDto(updatedProfile);
        } else {
            logger.warn("ID.me verification cannot be re-initiated for SellerProfile ID {} in status: {}", profile.getId(), profile.getIdMeStatus());
            throw new IllegalStateException("ID.me verification cannot be initiated for the current status: " + profile.getIdMeStatus());
        }
    }

    @Transactional
    public SellerProfileDto handleIdMeStatusUpdate(IdMeWebhookPayload payload) {
        SellerProfile profile = getSellerProfileEntityById(payload.getSellerProfileId());
        logger.info("Handling ID.me webhook for SellerProfile ID {}. New Status: {}", profile.getId(), payload.getStatus());

        profile.setIdMeStatus(payload.getStatus());
        profile.setIdMeExternalId(payload.getIdMeExternalId());
        if (StringUtils.hasText(payload.getReason())) {
            profile.setStatusReason("ID.me: " + payload.getReason());
        }
        if (StringUtils.hasText(payload.getVerificationDetailsLink())) {
            profile.setIdMeVerificationDetailsLink(payload.getVerificationDetailsLink());
        }

        if (payload.getStatus() == IdMeStatus.APPROVED) {
            profile.setOverallStatus(SellerOverallStatus.PENDING_LMS);
            notificationService.sendIdMeApprovedPendingLms(profile);
            initiateLmsInvitation(profile);
        } else if (payload.getStatus() == IdMeStatus.DECLINED) {
            profile.setOverallStatus(SellerOverallStatus.REJECTED_ID_ME);
            notificationService.sendIdMeDeclined(profile, payload.getReason());
        }

        SellerProfile updatedProfile = sellerProfileRepository.save(profile);
        return sellerProfileMapper.toDto(updatedProfile);
    }

    @Transactional
    protected void initiateLmsInvitation(SellerProfile profile) {
        if (profile.getLmsStatus() == LmsStatus.NOT_STARTED || profile.getLmsStatus() == LmsStatus.FAILED) {
            profile.setLmsStatus(LmsStatus.INVITATION_SENT);
            String lmsUrl = "https://lms.example.com/training/" + UUID.randomUUID().toString();
            profile.setLmsInvitationUrl(lmsUrl);
            notificationService.sendLmsInvitation(profile, lmsUrl);
            logger.info("LMS invitation sent for SellerProfile ID {}. URL: {}", profile.getId(), lmsUrl);
        }
    }

    @Transactional
    public SellerProfileDto handleLmsCompletionUpdate(LmsWebhookPayload payload) {
        SellerProfile profile = getSellerProfileEntityById(payload.getSellerProfileId());
        logger.info("Handling LMS webhook for SellerProfile ID {}. New Status: {}", profile.getId(), payload.getStatus());

        profile.setLmsStatus(payload.getStatus());
        profile.setLmsExternalId(payload.getLmsExternalId());
        if (StringUtils.hasText(payload.getCourseName())) {
            profile.setLmsCourseName(payload.getCourseName());
        }
        if (StringUtils.hasText(payload.getCompletionDate())) {
            profile.setLmsCompletionDateDetails(payload.getCompletionDate());
        }

        if (payload.getStatus() == LmsStatus.COMPLETED) {
            if (profile.getIdMeStatus() == IdMeStatus.APPROVED) {
                profile.setOverallStatus(SellerOverallStatus.ACTIVE);
                profile.setStatusReason("ID.me and LMS completed. Account is now active.");
                notificationService.sendLmsCompletedSellerActive(profile);
            } else {
                profile.setOverallStatus(SellerOverallStatus.NEEDS_ATTENTION);
                profile.setStatusReason("LMS completed, but ID.me status is not APPROVED (" + profile.getIdMeStatus() + "). Requires operator review.");
            }
        } else if (payload.getStatus() == LmsStatus.FAILED) {
            profile.setOverallStatus(SellerOverallStatus.REJECTED_LMS);
            notificationService.sendLmsFailed(profile, payload.getReason());
        }

        SellerProfile updatedProfile = sellerProfileRepository.save(profile);
        return sellerProfileMapper.toDto(updatedProfile);
    }
}
