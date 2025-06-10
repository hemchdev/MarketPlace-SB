package com.estuate.mpreplica.events;

import com.estuate.mpreplica.entity.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when an entire order's status is moved to DELIVERED.
 * This event decouples the order finalization logic from the remittance/ledger creation process.
 */
@Getter
public class OrderDeliveredEvent extends ApplicationEvent {
    private final Order order;

    public OrderDeliveredEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
}
