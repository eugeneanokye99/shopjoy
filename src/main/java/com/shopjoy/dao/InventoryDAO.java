package com.shopjoy.dao;

import com.shopjoy.model.Inventory;
import com.shopjoy.util.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Inventory entity. Implements CRUD and inventory-specific operations.
 */
public class InventoryDAO implements GenericDAO<Inventory, Integer> {

    @Override
    public Inventory findById(Integer inventoryId) throws SQLException {
        if (inventoryId == null) return null;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT inventory_id, product_id, quantity_in_stock, reorder_level, warehouse_location, last_restocked, updated_at FROM inventory WHERE inventory_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, inventoryId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapResultSetToInventory(rs);
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
    public List<Inventory> findAll() throws SQLException {
        List<Inventory> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT inventory_id, product_id, quantity_in_stock, reorder_level, warehouse_location, last_restocked, updated_at FROM inventory";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToInventory(rs));
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
    public Inventory save(Inventory inventory) throws SQLException {
        if (inventory == null) return null;
        if (inventory.getQuantityInStock() < 0) throw new IllegalArgumentException("quantity_in_stock cannot be negative");

        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "INSERT INTO inventory (product_id, quantity_in_stock, reorder_level, warehouse_location, last_restocked, updated_at) VALUES (?, ?, ?, ?, ?, ?) RETURNING inventory_id";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, inventory.getProductId());
                ps.setInt(2, inventory.getQuantityInStock());
                ps.setInt(3, inventory.getReorderLevel());
                ps.setString(4, inventory.getWarehouseLocation());
                ps.setObject(5, inventory.getLastRestocked());
                ps.setObject(6, inventory.getUpdatedAt());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) inventory.setInventoryId(rs.getInt("inventory_id"));
                }
            }
            return inventory;
        } catch (SQLException e) {
            System.out.println("save SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public Inventory update(Inventory inventory) throws SQLException {
        if (inventory == null) return null;
        if (inventory.getQuantityInStock() < 0) throw new IllegalArgumentException("quantity_in_stock cannot be negative");

        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "UPDATE inventory SET quantity_in_stock = ?, reorder_level = ?, warehouse_location = ?, updated_at = CURRENT_TIMESTAMP WHERE inventory_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, inventory.getQuantityInStock());
                ps.setInt(2, inventory.getReorderLevel());
                ps.setString(3, inventory.getWarehouseLocation());
                ps.setInt(4, inventory.getInventoryId());
                ps.executeUpdate();
            }
            return inventory;
        } catch (SQLException e) {
            System.out.println("update SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public boolean delete(Integer inventoryId) throws SQLException {
        if (inventoryId == null) return false;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "DELETE FROM inventory WHERE inventory_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, inventoryId);
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
            String sql = "SELECT COUNT(*) AS cnt FROM inventory";
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

    public Inventory findByProductId(int productId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT inventory_id, product_id, quantity_in_stock, reorder_level, warehouse_location, last_restocked, updated_at FROM inventory WHERE product_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapResultSetToInventory(rs);
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("findByProductId SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public boolean updateStock(int productId, int quantity) throws SQLException {
        if (quantity < 0) throw new IllegalArgumentException("quantity cannot be negative");
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "UPDATE inventory SET quantity_in_stock = ?, updated_at = CURRENT_TIMESTAMP WHERE product_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, quantity);
                ps.setInt(2, productId);
                int affected = ps.executeUpdate();
                return affected > 0;
            }
        } catch (SQLException e) {
            System.out.println("updateStock SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public boolean incrementStock(int productId, int amount) throws SQLException {
        if (amount < 0) throw new IllegalArgumentException("amount cannot be negative");
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "UPDATE inventory SET quantity_in_stock = quantity_in_stock + ?, updated_at = CURRENT_TIMESTAMP WHERE product_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, amount);
                ps.setInt(2, productId);
                int affected = ps.executeUpdate();
                return affected > 0;
            }
        } catch (SQLException e) {
            System.out.println("incrementStock SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public boolean decrementStock(int productId, int amount) throws SQLException {
        if (amount < 0) throw new IllegalArgumentException("amount cannot be negative");
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String checkSql = "SELECT quantity_in_stock FROM inventory WHERE product_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return false; // no inventory row
                    int current = rs.getInt("quantity_in_stock");
                    if (current < amount) return false; // insufficient
                }
            }

            String updateSql = "UPDATE inventory SET quantity_in_stock = quantity_in_stock - ?, updated_at = CURRENT_TIMESTAMP WHERE product_id = ?";
            try (PreparedStatement ps2 = conn.prepareStatement(updateSql)) {
                ps2.setInt(1, amount);
                ps2.setInt(2, productId);
                int affected = ps2.executeUpdate();
                return affected > 0;
            }
        } catch (SQLException e) {
            System.out.println("decrementStock SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public List<Inventory> findLowStockItems() throws SQLException {
        List<Inventory> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT inventory_id, product_id, quantity_in_stock, reorder_level, warehouse_location, last_restocked, updated_at FROM inventory WHERE quantity_in_stock <= reorder_level";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToInventory(rs));
            }
            return list;
        } catch (SQLException e) {
            System.out.println("findLowStockItems SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public List<Inventory> findOutOfStockItems() throws SQLException {
        List<Inventory> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT inventory_id, product_id, quantity_in_stock, reorder_level, warehouse_location, last_restocked, updated_at FROM inventory WHERE quantity_in_stock = 0";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToInventory(rs));
            }
            return list;
        } catch (SQLException e) {
            System.out.println("findOutOfStockItems SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public boolean checkStockAvailability(int productId, int requestedQuantity) throws SQLException {
        if (requestedQuantity < 0) return false;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT quantity_in_stock FROM inventory WHERE product_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt("quantity_in_stock") >= requestedQuantity;
                    return false;
                }
            }
        } catch (SQLException e) {
            System.out.println("checkStockAvailability SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public List<Inventory> findByWarehouse(String warehouseLocation) throws SQLException {
        List<Inventory> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT inventory_id, product_id, quantity_in_stock, reorder_level, warehouse_location, last_restocked, updated_at FROM inventory WHERE warehouse_location = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, warehouseLocation);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSetToInventory(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            System.out.println("findByWarehouse SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public boolean updateLastRestocked(int productId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "UPDATE inventory SET last_restocked = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE product_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, productId);
                int affected = ps.executeUpdate();
                return affected > 0;
            }
        } catch (SQLException e) {
            System.out.println("updateLastRestocked SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    private Inventory mapResultSetToInventory(ResultSet rs) throws SQLException {
        Inventory inv = new Inventory();
        inv.setInventoryId(rs.getInt("inventory_id"));
        inv.setProductId(rs.getInt("product_id"));
        inv.setQuantityInStock(rs.getInt("quantity_in_stock"));
        inv.setReorderLevel(rs.getInt("reorder_level"));
        inv.setWarehouseLocation(rs.getString("warehouse_location"));
        Timestamp last = rs.getTimestamp("last_restocked");
        if (last != null) inv.setLastRestocked(last.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) inv.setUpdatedAt(updated.toLocalDateTime());
        return inv;
    }
}
