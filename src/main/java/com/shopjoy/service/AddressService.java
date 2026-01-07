package com.shopjoy.service;

import com.shopjoy.dao.AddressDAO;
import com.shopjoy.model.Address;
import com.shopjoy.model.AddressType;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service handling user address management and default address logic.
 *
 * Default address logic:
 * - When a user adds their first address, it is automatically marked as default.
 * - There can be one default address per AddressType (SHIPPING or BILLING).
 * - When setting a new default address, the DAO clears the previous default
 *   for that user/type within a database transaction.
 * - When deleting a default address and other addresses exist, the service
 *   promotes another address to default to ensure the user retains a default.
 */
public class AddressService {
    private final AddressDAO addressDAO;

    public AddressService() {
        this.addressDAO = new AddressDAO();
    }

    /**
     * Return addresses for a user.
     */
    public List<Address> getUserAddresses(int userId) {
        if (userId <= 0) return List.of();
        try {
            return addressDAO.findByUserId(userId);
        } catch (SQLException e) {
            System.err.println("getUserAddresses: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Get address by id or null.
     */
    public Address getAddressById(int addressId) {
        if (addressId <= 0) return null;
        try {
            return addressDAO.findById(addressId);
        } catch (SQLException e) {
            System.err.println("getAddressById: " + e.getMessage());
            return null;
        }
    }

    /**
     * Add address. If it's the user's first address, mark as default.
     */
    public Address addAddress(Address address) {
        if (!isValidAddress(address)) {
            System.err.println("addAddress: invalid address");
            return null;
        }
        try {
            List<Address> existing = addressDAO.findByUserId(address.getUserId());
            boolean first = existing == null || existing.isEmpty();
            if (first) address.setDefault(true);
            address.setCreatedAt(LocalDateTime.now());
            Address saved = addressDAO.save(address);
            // If saved and marked default, ensure DAO set it properly (DAO.save stores is_default flag)
            return saved;
        } catch (SQLException e) {
            System.err.println("addAddress: " + e.getMessage());
            return null;
        }
    }

    /**
     * Update an address. Returns updated address or null.
     */
    public Address updateAddress(Address address) {
        if (address == null || address.getAddressId() <= 0) return null;
        if (!isValidAddress(address)) return null;
        try {
            return addressDAO.update(address);
        } catch (SQLException e) {
            System.err.println("updateAddress: " + e.getMessage());
            return null;
        }
    }

    /**
     * Delete an address. If deleting a default address and other addresses exist,
     * promote another address to default to preserve user's default address.
     */
    public boolean deleteAddress(int addressId) {
        if (addressId <= 0) return false;
        try {
            Address addr = addressDAO.findById(addressId);
            if (addr == null) return false;
            int userId = addr.getUserId();
            boolean wasDefault = addr.isDefault();

            boolean deleted = addressDAO.delete(addressId);
            if (!deleted) return false;

            if (wasDefault) {
                List<Address> rest = addressDAO.findByUserId(userId);
                if (rest != null && !rest.isEmpty()) {
                    // promote the first address of same type to default
                    Address candidate = null;
                    for (Address a : rest) {
                        if (a.getAddressType() == addr.getAddressType()) {
                            candidate = a;
                            break;
                        }
                    }
                    if (candidate == null) candidate = rest.get(0);
                    try {
                        addressDAO.setDefaultAddress(candidate.getAddressId(), userId, candidate.getAddressType());
                    } catch (SQLException e) {
                        System.err.println("deleteAddress: failed to set new default: " + e.getMessage());
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("deleteAddress: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get default shipping address for user.
     */
    public Address getDefaultShippingAddress(int userId) {
        if (userId <= 0) return null;
        try {
            return addressDAO.findDefaultAddress(userId, AddressType.SHIPPING);
        } catch (SQLException e) {
            System.err.println("getDefaultShippingAddress: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get default billing address for user.
     */
    public Address getDefaultBillingAddress(int userId) {
        if (userId <= 0) return null;
        try {
            return addressDAO.findDefaultAddress(userId, AddressType.BILLING);
        } catch (SQLException e) {
            System.err.println("getDefaultBillingAddress: " + e.getMessage());
            return null;
        }
    }

    /**
     * Set an address as default for its type. Returns true on success.
     */
    public boolean setDefaultAddress(int addressId, int userId) {
        if (addressId <= 0 || userId <= 0) return false;
        try {
            Address a = addressDAO.findById(addressId);
            if (a == null) return false;
            AddressType type = a.getAddressType();
            if (type == null) return false;
            return addressDAO.setDefaultAddress(addressId, userId, type);
        } catch (SQLException e) {
            System.err.println("setDefaultAddress: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get shipping addresses for a user.
     */
    public List<Address> getShippingAddresses(int userId) {
        if (userId <= 0) return List.of();
        try {
            return addressDAO.findShippingAddresses(userId);
        } catch (SQLException e) {
            System.err.println("getShippingAddresses: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Get billing addresses for a user.
     */
    public List<Address> getBillingAddresses(int userId) {
        if (userId <= 0) return List.of();
        try {
            return addressDAO.findBillingAddresses(userId);
        } catch (SQLException e) {
            System.err.println("getBillingAddresses: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Delete all addresses for a user.
     */
    public boolean deleteAllUserAddresses(int userId) {
        if (userId <= 0) return false;
        try {
            return addressDAO.deleteUserAddresses(userId);
        } catch (SQLException e) {
            System.err.println("deleteAllUserAddresses: " + e.getMessage());
            return false;
        }
    }

    // --- Validation helpers ---

    /**
     * Basic postal code validation for a few countries; falls back to length check.
     */
    public boolean isValidPostalCode(String postalCode, String country) {
        if (postalCode == null || postalCode.trim().isEmpty()) return false;
        String pc = postalCode.trim();
        if (country == null) country = "";
        String c = country.trim().toUpperCase();

        try {
            if (c.contains("US") || c.contains("UNITED STATES") || c.equals("US")) {
                return pc.matches("\\d{5}(-\\d{4})?");
            }
            if (c.contains("CA") || c.contains("CANADA") || c.equals("CA")) {
                return pc.matches("[A-Za-z]\\d[A-Za-z] ?\\d[A-Za-z]\\d");
            }
            if (c.contains("GB") || c.contains("UNITED KINGDOM") || c.contains("UK")) {
                return pc.matches("[A-Za-z]{1,2}\\d{1,2} ?\\d[A-Za-z]{2}");
            }
        } catch (Exception ignored) {}

        // fallback: reasonable length
        return pc.length() >= 3 && pc.length() <= 10;
    }

    /**
     * Validate required fields for an Address.
     */
    public boolean isValidAddress(Address address) {
        if (address == null) return false;
        if (address.getUserId() <= 0) return false;
        if (address.getStreetAddress() == null || address.getStreetAddress().trim().isEmpty()) return false;
        if (address.getStreetAddress().length() > 255) return false;
        if (address.getCity() == null || address.getCity().trim().isEmpty()) return false;
        if (address.getCity().length() > 100) return false;
        if (address.getCountry() == null || address.getCountry().trim().isEmpty()) return false;
        if (address.getPostalCode() != null && !address.getPostalCode().trim().isEmpty()) {
            if (!isValidPostalCode(address.getPostalCode(), address.getCountry())) return false;
        }
        return true;
    }
}
