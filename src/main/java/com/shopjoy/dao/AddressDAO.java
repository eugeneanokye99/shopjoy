package com.shopjoy.dao;

import com.shopjoy.model.Address;
import com.shopjoy.model.AddressType;
import com.shopjoy.util.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Address entity. Implements CRUD and address-specific operations.
 */
public class AddressDAO implements GenericDAO<Address, Integer> {

    @Override
    public Address findById(Integer addressId) throws SQLException {
        if (addressId == null) return null;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT address_id, user_id, address_type, street_address, city, state, postal_code, country, is_default, created_at FROM addresses WHERE address_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, addressId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapResultSetToAddress(rs);
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("findById SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public List<Address> findAll() throws SQLException {
        List<Address> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT address_id, user_id, address_type, street_address, city, state, postal_code, country, is_default, created_at FROM addresses";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToAddress(rs));
            }
            return list;
        } catch (SQLException e) {
            System.out.println("findAll SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public Address save(Address address) throws SQLException {
        if (address == null) return null;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "INSERT INTO addresses (user_id, address_type, street_address, city, state, postal_code, country, is_default) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING address_id";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, address.getUserId());
                ps.setString(2, address.getAddressType() != null ? address.getAddressType().name().toLowerCase() : null);
                ps.setString(3, address.getStreetAddress());
                ps.setString(4, address.getCity());
                ps.setString(5, address.getState());
                ps.setString(6, address.getPostalCode());
                ps.setString(7, address.getCountry());
                ps.setBoolean(8, address.isDefault());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) address.setAddressId(rs.getInt("address_id"));
                }
            }
            return address;
        } catch (SQLException e) {
            System.out.println("save SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public Address update(Address address) throws SQLException {
        if (address == null) return null;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "UPDATE addresses SET street_address = ?, city = ?, state = ?, postal_code = ?, country = ?, is_default = ? WHERE address_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, address.getStreetAddress());
                ps.setString(2, address.getCity());
                ps.setString(3, address.getState());
                ps.setString(4, address.getPostalCode());
                ps.setString(5, address.getCountry());
                ps.setBoolean(6, address.isDefault());
                ps.setInt(7, address.getAddressId());
                ps.executeUpdate();
            }
            return address;
        } catch (SQLException e) {
            System.out.println("update SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public boolean delete(Integer addressId) throws SQLException {
        if (addressId == null) return false;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "DELETE FROM addresses WHERE address_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, addressId);
                int affected = ps.executeUpdate();
                return affected > 0;
            }
        } catch (SQLException e) {
            System.out.println("delete SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public long count() throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT COUNT(*) AS cnt FROM addresses";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("cnt");
                return 0L;
            }
        } catch (SQLException e) {
            System.out.println("count SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    // --- Custom methods ---

    public List<Address> findByUserId(int userId) throws SQLException {
        List<Address> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT address_id, user_id, address_type, street_address, city, state, postal_code, country, is_default, created_at FROM addresses WHERE user_id = ? ORDER BY is_default DESC, created_at DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSetToAddress(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            System.out.println("findByUserId SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public Address findDefaultAddress(int userId, AddressType type) throws SQLException {
        if (type == null) return null;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT address_id, user_id, address_type, street_address, city, state, postal_code, country, is_default, created_at FROM addresses WHERE user_id = ? AND address_type = ? AND is_default = true";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setString(2, type.name());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapResultSetToAddress(rs);
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("findDefaultAddress SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public boolean setDefaultAddress(int addressId, int userId, AddressType type) throws SQLException {
        if (type == null) return false;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            conn.setAutoCommit(false);
            String clearSql = "UPDATE addresses SET is_default = false WHERE user_id = ? AND address_type = ?";
            try (PreparedStatement clearPs = conn.prepareStatement(clearSql)) {
                clearPs.setInt(1, userId);
                clearPs.setString(2, type.name());
                clearPs.executeUpdate();
            }

            String setSql = "UPDATE addresses SET is_default = true WHERE address_id = ?";
            try (PreparedStatement setPs = conn.prepareStatement(setSql)) {
                setPs.setInt(1, addressId);
                int affected = setPs.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            System.out.println("setDefaultAddress SQLException: " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public List<Address> findShippingAddresses(int userId) throws SQLException {
        return findByTypeForUser(userId, AddressType.SHIPPING);
    }

    public List<Address> findBillingAddresses(int userId) throws SQLException {
        return findByTypeForUser(userId, AddressType.BILLING);
    }

    private List<Address> findByTypeForUser(int userId, AddressType type) throws SQLException {
        List<Address> list = new ArrayList<>();
        if (type == null) return list;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT address_id, user_id, address_type, street_address, city, state, postal_code, country, is_default, created_at FROM addresses WHERE user_id = ? AND address_type = ? ORDER BY is_default DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setString(2, type.name());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSetToAddress(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            System.out.println("findByTypeForUser SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public boolean deleteUserAddresses(int userId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "DELETE FROM addresses WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                int affected = ps.executeUpdate();
                return affected > 0;
            }
        } catch (SQLException e) {
            System.out.println("deleteUserAddresses SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    private Address mapResultSetToAddress(ResultSet rs) throws SQLException {
        Address a = new Address();
        a.setAddressId(rs.getInt("address_id"));
        a.setUserId(rs.getInt("user_id"));
        String typeStr = rs.getString("address_type");
        if (typeStr != null) {
            try {
                a.setAddressType(AddressType.valueOf(typeStr));
            } catch (IllegalArgumentException ignored) {
                a.setAddressType(null);
            }
        }
        a.setStreetAddress(rs.getString("street_address"));
        a.setCity(rs.getString("city"));
        a.setState(rs.getString("state"));
        a.setPostalCode(rs.getString("postal_code"));
        a.setCountry(rs.getString("country"));
        a.setDefault(rs.getBoolean("is_default"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) a.setCreatedAt(created.toLocalDateTime());
        return a;
    }
}
