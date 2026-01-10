package com.shopjoy.service;

import com.shopjoy.dao.InventoryDAO;
import com.shopjoy.dao.ProductDAO;
import com.shopjoy.model.Inventory;
import com.shopjoy.model.Product;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for inventory management and stock operations.
 * Note: methods that modify stock are synchronized to reduce race conditions
 * in concurrent environments. For heavier concurrency requirements consider
 * using database transactions/row-level locking.
 */
public class InventoryService {
    private final InventoryDAO inventoryDAO;
    private final ProductDAO productDAO;

    public InventoryService() {
        this.inventoryDAO = new InventoryDAO();
        this.productDAO = new ProductDAO();
    }

    /**
     * Get inventory record for a product.
     */
    public Inventory getInventoryByProductId(int productId) {
        if (productId <= 0)
            return null;
        try {
            return inventoryDAO.findByProductId(productId);
        } catch (SQLException e) {
            System.err.println("getInventoryByProductId: " + e.getMessage());
            return null;
        }
    }

    /**
     * Return all inventory records.
     */
    public List<Inventory> getAllInventory() {
        try {
            return inventoryDAO.findAll();
        } catch (SQLException e) {
            System.err.println("getAllInventory: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Set stock to an exact quantity. Quantity must be >= 0.
     */
    public synchronized boolean updateStock(int productId, int newQuantity) {
        if (productId <= 0 || newQuantity < 0)
            return false;
        try {
            return inventoryDAO.updateStock(productId, newQuantity);
        } catch (SQLException e) {
            System.err.println("updateStock: " + e.getMessage());
            return false;
        }
    }

    /**
     * Add stock amount (>0) and update last restocked timestamp.
     */
    public synchronized boolean addStock(int productId, int amount) {
        if (productId <= 0 || amount <= 0)
            return false;
        try {
            boolean ok = inventoryDAO.incrementStock(productId, amount);
            if (!ok)
                return false;
            inventoryDAO.updateLastRestocked(productId);
            return true;
        } catch (SQLException e) {
            System.err.println("addStock: " + e.getMessage());
            return false;
        }
    }

    /**
     * Remove stock amount (>0). Prevents negative stock.
     */
    public synchronized boolean removeStock(int productId, int amount) {
        if (productId <= 0 || amount <= 0)
            return false;
        try {
            // check availability
            Inventory inv = inventoryDAO.findByProductId(productId);
            if (inv == null)
                return false;
            if (inv.getQuantityInStock() < amount)
                return false;
            return inventoryDAO.decrementStock(productId, amount);
        } catch (SQLException e) {
            System.err.println("removeStock: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns true if requestedQuantity is available.
     */
    public boolean isStockAvailable(int productId, int requestedQuantity) {
        if (productId <= 0 || requestedQuantity < 0)
            return false;
        try {
            return inventoryDAO.checkStockAvailability(productId, requestedQuantity);
        } catch (SQLException e) {
            System.err.println("isStockAvailable: " + e.getMessage());
            return false;
        }
    }

    /**
     * Return products that need restocking (quantity <= reorder level).
     */
    public List<ProductWithStock> getLowStockProducts() {
        List<ProductWithStock> out = new ArrayList<>();
        try {
            List<Inventory> items = inventoryDAO.findLowStockItems();
            if (items == null)
                return out;
            for (Inventory inv : items) {
                Product p = productDAO.findById(inv.getProductId());
                out.add(new ProductWithStock(p, inv));
            }
            return out;
        } catch (SQLException e) {
            System.err.println("getLowStockProducts: " + e.getMessage());
            return out;
        }
    }

    /**
     * Return out-of-stock products (quantity == 0).
     */
    public List<ProductWithStock> getOutOfStockProducts() {
        List<ProductWithStock> out = new ArrayList<>();
        try {
            List<Inventory> items = inventoryDAO.findOutOfStockItems();
            if (items == null)
                return out;
            for (Inventory inv : items) {
                Product p = productDAO.findById(inv.getProductId());
                out.add(new ProductWithStock(p, inv));
            }
            return out;
        } catch (SQLException e) {
            System.err.println("getOutOfStockProducts: " + e.getMessage());
            return out;
        }
    }

    /**
     * Restock a product by quantity and optionally set warehouse location.
     */
    public synchronized boolean restockProduct(int productId, int quantity, String warehouseLocation) {
        if (productId <= 0 || quantity <= 0)
            return false;
        try {
            boolean added = addStock(productId, quantity);
            if (!added)
                return false;
            if (warehouseLocation != null && !warehouseLocation.trim().isEmpty()) {
                Inventory inv = inventoryDAO.findByProductId(productId);
                if (inv == null)
                    return false;
                inv.setWarehouseLocation(warehouseLocation.trim());
                inventoryDAO.update(inv);
            }
            inventoryDAO.updateLastRestocked(productId);
            return true;
        } catch (SQLException e) {
            System.err.println("restockProduct: " + e.getMessage());
            return false;
        }
    }

    /**
     * Calculate total stock value using product cost prices.
     * Stock value = sum(quantity * cost_price) for all inventory items.
     */
    public int getTotalStockValue() {
        double total = 0.0;
        try {
            List<Inventory> items = inventoryDAO.findAll();
            if (items == null)
                return 0;
            for (Inventory inv : items) {
                Product p = productDAO.findById(inv.getProductId());
                if (p == null)
                    continue;
                total += ((double) inv.getQuantityInStock()) * p.getCostPrice();
            }
            return (int) Math.round(total);
        } catch (SQLException e) {
            System.err.println("getTotalStockValue: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Return inventory records for a warehouse location.
     */
    public List<Inventory> getInventoryByWarehouse(String warehouseLocation) {
        if (warehouseLocation == null || warehouseLocation.trim().isEmpty())
            return new ArrayList<>();
        try {
            return inventoryDAO.findByWarehouse(warehouseLocation.trim());
        } catch (SQLException e) {
            System.err.println("getInventoryByWarehouse: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Update reorder level for a product's inventory.
     */
    public synchronized boolean updateReorderLevel(int productId, int newReorderLevel) {
        if (productId <= 0 || newReorderLevel < 0)
            return false;
        try {
            Inventory inv = inventoryDAO.findByProductId(productId);
            if (inv == null)
                return false;
            inv.setReorderLevel(newReorderLevel);
            inventoryDAO.update(inv);
            return true;
        } catch (SQLException e) {
            System.err.println("updateReorderLevel: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update warehouse location for a product's inventory.
     */
    public synchronized boolean updateWarehouseLocation(int productId, String location) {
        if (productId <= 0)
            return false;
        try {
            Inventory inv = inventoryDAO.findByProductId(productId);
            if (inv == null)
                return false;
            inv.setWarehouseLocation(location);
            return inventoryDAO.update(inv) != null;
        } catch (SQLException e) {
            System.err.println("updateWarehouseLocation: " + e.getMessage());
            return false;
        }
    }

    /**
     * Build an inventory summary with counts and total value.
     */
    public InventorySummary getInventorySummary() {
        try {
            List<Inventory> all = inventoryDAO.findAll();
            int totalProducts = all == null ? 0 : all.size();
            int low = inventoryDAO.findLowStockItems().size();
            int out = inventoryDAO.findOutOfStockItems().size();
            double totalValue = 0.0;
            if (all != null) {
                for (Inventory inv : all) {
                    Product p = productDAO.findById(inv.getProductId());
                    if (p != null)
                        totalValue += inv.getQuantityInStock() * p.getCostPrice();
                }
            }
            return new InventorySummary(totalProducts, low, out, totalValue);
        } catch (SQLException e) {
            System.err.println("getInventorySummary: " + e.getMessage());
            return new InventorySummary(0, 0, 0, 0.0);
        }
    }

    /**
     * Returns true if product is at or below reorder level and should alert.
     */
    public boolean checkAndAlertLowStock(int productId) {
        if (productId <= 0)
            return false;
        try {
            Inventory inv = inventoryDAO.findByProductId(productId);
            if (inv == null)
                return false;
            return inv.getQuantityInStock() <= inv.getReorderLevel();
        } catch (SQLException e) {
            System.err.println("checkAndAlertLowStock: " + e.getMessage());
            return false;
        }
    }
}
