package com.shopjoy.dao;

import com.shopjoy.model.Inventory;
import com.shopjoy.util.DbConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Inventory entity. Implements CRUD and inventory-specific operations.
 */
public class InventoryDAO implements GenericDAO<Inventory, Integer> {

    @Override
    public Inventory findById(Integer inventoryId) throws SQLException {
        if (inventoryId == null) return null;

        String sql = "SELECT inventory_id, product_id, quantity_in_stock, reorder_level, warehouse_location, " +
                "last_restocked, updated_at FROM inventory WHERE inventory_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, inventoryId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToInventory(rs) : null;
            }
        }
    }

    @Override
    public List<Inventory> findAll() throws SQLException {
        List<Inventory> list = new ArrayList<>();
        String sql = "SELECT inventory_id, product_id, quantity_in_stock, reorder_level, warehouse_location, " +
                "last_restocked, updated_at FROM inventory";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapResultSetToInventory(rs));
        }
        return list;
    }

    @Override
    public Inventory save(Inventory inventory) throws SQLException {
        if (inventory == null) return null;
        if (inventory.getQuantityInStock() < 0) throw new IllegalArgumentException("quantity_in_stock cannot be negative");

        String sql = "INSERT INTO inventory (product_id, quantity_in_stock, reorder_level, warehouse_location, " +
                "last_restocked, updated_at) VALUES (?, ?, ?, ?, ?, ?) RETURNING inventory_id";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
    }

    @Override
    public Inventory update(Inventory inventory) throws SQLException {
        if (inventory == null) return null;
        if (inventory.getQuantityInStock() < 0) throw new IllegalArgumentException("quantity_in_stock cannot be negative");

        String sql = "UPDATE inventory SET quantity_in_stock = ?, reorder_level = ?, warehouse_location = ?, " +
                "updated_at = CURRENT_TIMESTAMP WHERE inventory_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, inventory.getQuantityInStock());
            ps.setInt(2, inventory.getReorderLevel());
            ps.setString(3, inventory.getWarehouseLocation());
            ps.setInt(4, inventory.getInventoryId());
            ps.executeUpdate();
        }
        return inventory;
    }

    @Override
    public boolean delete(Integer inventoryId) throws SQLException {
        if (inventoryId == null) return false;

        String sql = "DELETE FROM inventory WHERE inventory_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, inventoryId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM inventory";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getLong("cnt") : 0L;
        }
    }

    // --- Custom methods ---

    public Inventory findByProductId(int productId) throws SQLException {
        String sql = "SELECT inventory_id, product_id, quantity_in_stock, reorder_level, warehouse_location, " +
                "last_restocked, updated_at FROM inventory WHERE product_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToInventory(rs) : null;
            }
        }
    }

    public boolean updateStock(int productId, int quantity) throws SQLException {
        if (quantity < 0) throw new IllegalArgumentException("quantity cannot be negative");

        String sql = "UPDATE inventory SET quantity_in_stock = ?, updated_at = CURRENT_TIMESTAMP WHERE product_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean incrementStock(int productId, int amount) throws SQLException {
        if (amount < 0) throw new IllegalArgumentException("amount cannot be negative");

        String sql = "UPDATE inventory SET quantity_in_stock = quantity_in_stock + ?, updated_at = CURRENT_TIMESTAMP WHERE product_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, amount);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean decrementStock(int productId, int amount) throws SQLException {
        if (amount < 0) throw new IllegalArgumentException("amount cannot be negative");

        String checkSql = "SELECT quantity_in_stock FROM inventory WHERE product_id = ?";
        String updateSql = "UPDATE inventory SET quantity_in_stock = quantity_in_stock - ?, updated_at = CURRENT_TIMESTAMP WHERE product_id = ?";

        try (Connection conn = DbConfig.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return false;
                    int current = rs.getInt("quantity_in_stock");
                    if (current < amount) return false;
                }
            }

            try (PreparedStatement ps2 = conn.prepareStatement(updateSql)) {
                ps2.setInt(1, amount);
                ps2.setInt(2, productId);
                return ps2.executeUpdate() > 0;
            }
        }
    }

    public List<Inventory> findLowStockItems() throws SQLException {
        List<Inventory> list = new ArrayList<>();
        String sql = "SELECT inventory_id, product_id, quantity_in_stock, reorder_level, warehouse_location, " +
                "last_restocked, updated_at FROM inventory WHERE quantity_in_stock <= reorder_level";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapResultSetToInventory(rs));
        }
        return list;
    }

    public List<Inventory> findOutOfStockItems() throws SQLException {
        List<Inventory> list = new ArrayList<>();
        String sql = "SELECT inventory_id, product_id, quantity_in_stock, reorder_level, warehouse_location, " +
                "last_restocked, updated_at FROM inventory WHERE quantity_in_stock = 0";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapResultSetToInventory(rs));
        }
        return list;
    }

    public boolean checkStockAvailability(int productId, int requestedQuantity) throws SQLException {
        if (requestedQuantity < 0) return false;

        String sql = "SELECT quantity_in_stock FROM inventory WHERE product_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("quantity_in_stock") >= requestedQuantity;
            }
        }
    }

    public List<Inventory> findByWarehouse(String warehouseLocation) throws SQLException {
        List<Inventory> list = new ArrayList<>();
        String sql = "SELECT inventory_id, product_id, quantity_in_stock, reorder_level, warehouse_location, " +
                "last_restocked, updated_at FROM inventory WHERE warehouse_location = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, warehouseLocation);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToInventory(rs));
            }
        }
        return list;
    }

    public boolean updateLastRestocked(int productId) throws SQLException {
        String sql = "UPDATE inventory SET last_restocked = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE product_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
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
