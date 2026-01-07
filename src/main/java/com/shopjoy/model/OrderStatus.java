package com.shopjoy.model;

public enum OrderStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Determine whether current status can transition to newStatus.
     * Terminal states: DELIVERED, CANCELLED
     */
    public boolean canTransitionTo(OrderStatus newStatus) {
        if (newStatus == null) return false;
        switch (this) {
            case PENDING:
                return newStatus == PROCESSING || newStatus == CANCELLED;
            case PROCESSING:
                return newStatus == SHIPPED || newStatus == CANCELLED;
            case SHIPPED:
                return newStatus == DELIVERED;
            case DELIVERED:
            case CANCELLED:
            default:
                return false;
        }
    }

    public static OrderStatus fromString(String value) {
        if (value == null) return null;
        String v = value.trim();
        try {
            return OrderStatus.valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            for (OrderStatus s : values()) {
                if (s.displayName.equalsIgnoreCase(v)) return s;
            }
            return null;
        }
    }

    @Override
    public String toString() { return name(); }
}
