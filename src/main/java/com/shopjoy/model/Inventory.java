package com.shopjoy.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Inventory {
    private int inventoryId;
    private int productId;
    private int quantityInStock;
    private int reorderLevel;
    private String warehouseLocation;
    private LocalDateTime lastRestocked;
    private LocalDateTime updatedAt;

    public Inventory() {}

    public Inventory(int productId, int quantityInStock, int reorderLevel, String warehouseLocation, LocalDateTime lastRestocked, LocalDateTime updatedAt) {
        this.productId = productId;
        setQuantityInStock(quantityInStock);
        this.reorderLevel = reorderLevel;
        this.warehouseLocation = warehouseLocation;
        this.lastRestocked = lastRestocked;
        this.updatedAt = updatedAt;
    }

    public Inventory(int inventoryId, int productId, int quantityInStock, int reorderLevel, String warehouseLocation, LocalDateTime lastRestocked, LocalDateTime updatedAt) {
        this.inventoryId = inventoryId;
        this.productId = productId;
        setQuantityInStock(quantityInStock);
        this.reorderLevel = reorderLevel;
        this.warehouseLocation = warehouseLocation;
        this.lastRestocked = lastRestocked;
        this.updatedAt = updatedAt;
    }

    public int getInventoryId() { return inventoryId; }
    public void setInventoryId(int inventoryId) { this.inventoryId = inventoryId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQuantityInStock() { return quantityInStock; }
    public void setQuantityInStock(int quantityInStock) {
        if (quantityInStock < 0) throw new IllegalArgumentException("quantityInStock must be >= 0");
        this.quantityInStock = quantityInStock;
    }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

    public String getWarehouseLocation() { return warehouseLocation; }
    public void setWarehouseLocation(String warehouseLocation) { this.warehouseLocation = warehouseLocation; }

    public LocalDateTime getLastRestocked() { return lastRestocked; }
    public void setLastRestocked(LocalDateTime lastRestocked) { this.lastRestocked = lastRestocked; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Inventory{" +
                "inventoryId=" + inventoryId +
                ", productId=" + productId +
                ", quantityInStock=" + quantityInStock +
                ", reorderLevel=" + reorderLevel +
                ", warehouseLocation='" + warehouseLocation + '\'' +
                ", lastRestocked=" + lastRestocked +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Inventory inventory = (Inventory) o;
        return inventoryId == inventory.inventoryId;
    }

    @Override
    public int hashCode() { return Objects.hash(inventoryId); }
}
