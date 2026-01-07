package com.shopjoy.model;

public enum AddressType {
    SHIPPING("Shipping"),
    BILLING("Billing");

    private final String displayName;

    AddressType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    public static AddressType fromString(String value) {
        if (value == null) return null;
        String v = value.trim();
        try {
            return AddressType.valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            for (AddressType t : values()) {
                if (t.displayName.equalsIgnoreCase(v)) return t;
            }
            return null;
        }
    }

    @Override
    public String toString() { return name(); }
}
