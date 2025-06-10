package com.estuate.mpreplica.events;

import com.estuate.mpreplica.entity.Payout;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a Payout's status is confirmed as COMPLETED,
 * typically after receiving a successful webhook notification from the PSP.
 * This event triggers downstream processes like PDF invoice generation.
 */
@Getter
public class PayoutCompletedEvent extends ApplicationEvent {
    private final Payout payout;

    public PayoutCompletedEvent(Object source, Payout payout) {
        super(source);
        this.payout = payout;
    }
}
