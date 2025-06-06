package com.estuate.mpreplica.enums;

public enum IdMeStatus {
    NOT_STARTED,
    PENDING,
    VERIFICATION_INITIATED, // Operator triggered, or seller started
    VERIFICATION_PENDING_USER_ACTION,
    APPROVED,
    DECLINED
}
