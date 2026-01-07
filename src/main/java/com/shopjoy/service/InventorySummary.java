package com.shopjoy.service;

/**
 * Summary of inventory metrics.
 */
public class InventorySummary {
    private final int totalProducts;
    private final int lowStockCount;
    private final int outOfStockCount;
    private final double totalInventoryValue;

    public InventorySummary(int totalProducts, int lowStockCount, int outOfStockCount, double totalInventoryValue) {
        this.totalProducts = totalProducts;
        this.lowStockCount = lowStockCount;
        this.outOfStockCount = outOfStockCount;
        this.totalInventoryValue = totalInventoryValue;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public int getLowStockCount() {
        return lowStockCount;
    }

    public int getOutOfStockCount() {
        return outOfStockCount;
    }

    public double getTotalInventoryValue() {
        return totalInventoryValue;
    }
}
