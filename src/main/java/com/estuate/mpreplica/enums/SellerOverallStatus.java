package com.estuate.mpreplica.enums;

public enum SellerOverallStatus {
    NOT_STARTED,
    PENDING_ID_ME,
    PENDING_LMS,
    ACTIVE,
    SUSPENDED,
    NEEDS_ATTENTION, // A general status if something is wrong
    REJECTED_ID_ME, // ID.me verification was declined
    REJECTED_LMS    // LMS was failed or not completed
}
