package com.shopjoy.model;

public enum PaymentStatus {
    UNPAID("Unpaid"),
    PAID("Paid"),
    REFUNDED("Refunded");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PaymentStatus fromString(String value) {
        if (value == null) return null;
        String v = value.trim();
        try {
            return PaymentStatus.valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            for (PaymentStatus s : values()) {
                if (s.displayName.equalsIgnoreCase(v)) return s;
            }
            return null;
        }
    }

    @Override
    public String toString() { return name(); }
}
