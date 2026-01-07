package com.shopjoy.model;

public enum UserType {
    CUSTOMER("Customer"),
    ADMIN("Admin");

    private final String displayName;

    UserType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static UserType fromString(String value) {
        if (value == null) return null;
        String v = value.trim();
        try {
            return UserType.valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            for (UserType t : values()) {
                if (t.displayName.equalsIgnoreCase(v)) return t;
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return name();
    }
}
