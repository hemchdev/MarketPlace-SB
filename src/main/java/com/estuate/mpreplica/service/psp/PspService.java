package com.estuate.mpreplica.service.psp;

import com.estuate.mpreplica.entity.Payout;
import java.util.List;

/**
 * An interface for Payment Service Provider (PSP) operations, abstracting the specific implementation
 * (e.g., PayPal, Stripe) from the core business logic.
 */
public interface PspService {
    /**
     * Initiates a batch payout to multiple recipients.
     *
     * @param payouts A list of Payout entities to be processed.
     * @return A unique batch ID from the PSP that identifies this entire payout request.
     * @throws Exception if the batch submission fails.
     */
    String createBatchPayout(List<Payout> payouts) throws Exception;

    /**
     * Verifies an incoming webhook to ensure it is authentic and from the PSP.
     *
     * @param headers The HTTP headers from the incoming request.
     * @param body The raw request body.
     * @return true if the webhook is valid, false otherwise.
     */
    boolean verifyWebhook(java.util.Map<String, String> headers, String body);
}
