package com.estuate.mpreplica.service;

import com.estuate.mpreplica.entity.CommissionTier;
import com.estuate.mpreplica.entity.SellerProfile;
import com.estuate.mpreplica.repository.CommissionTierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Optional;

/**
 * Service responsible for calculating the applicable commission rate for a seller
 * based on a defined hierarchy of rules.
 */
@Service
public class CommissionService {

    private static final Logger logger = LoggerFactory.getLogger(CommissionService.class);

    @Autowired
    private CommissionTierRepository commissionTierRepository;

    @Autowired
    private PlatformConfigurationService platformConfigurationService;

    /**
     * Determines the applicable commission rate for a given seller profile.
     * The logic follows a specific hierarchy:
     * 1. A manually set override rate on the seller's profile.
     * 2. The best-matching active commission tier the seller qualifies for based on their rating.
     * 3. A system-wide default commission rate as a final fallback.
     *
     * @param seller The SellerProfile for which to calculate the commission rate.
     * @return The applicable commission rate as a BigDecimal.
     */
    public BigDecimal getApplicableCommissionRate(SellerProfile seller) {
        // 1. Check for a direct operator override.
        if (seller.getCommissionRateOverride() != null) {
            logger.debug("Applying commission override rate {} for seller ID {}", seller.getCommissionRateOverride(), seller.getId());
            return seller.getCommissionRateOverride();
        }

        // 2. Find the best-matching active commission tier.
        Double sellerRating = seller.getCurrentRating();
        if (sellerRating != null) {
            // Find the active tier with the highest rating requirement that the seller meets.
            Optional<CommissionTier> bestTier = commissionTierRepository.findByIsActiveTrue()
                    .stream()
                    .filter(tier -> sellerRating >= tier.getMinRatingRequired())
                    .max(Comparator.comparing(CommissionTier::getMinRatingRequired));

            if (bestTier.isPresent()) {
                CommissionTier appliedTier = bestTier.get();
                logger.debug("Applying commission tier '{}' with rate {} for seller ID {} (rating: {})",
                        appliedTier.getTierName(), appliedTier.getCommissionRate(), seller.getId(), sellerRating);
                return appliedTier.getCommissionRate();
            }
        }

        // 3. Fallback to the system-wide default commission rate.
        BigDecimal defaultRate = platformConfigurationService.getBaseCommissionRate();
        logger.debug("Applying default platform commission rate {} for seller ID {}", defaultRate, seller.getId());
        return defaultRate;
    }
}

