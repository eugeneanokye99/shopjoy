package com.shopjoy.service;

import com.shopjoy.model.Inventory;
import com.shopjoy.model.Product;

/**
 * DTO combining a Product with its Inventory details.
 */
public class ProductWithStock {
    private final Product product;
    private final Inventory inventory;
    private final boolean needsRestock;

    public ProductWithStock(Product product, Inventory inventory) {
        this.product = product;
        this.inventory = inventory;
        this.needsRestock = inventory != null && inventory.getQuantityInStock() <= inventory.getReorderLevel();
    }

    public Product getProduct() {
        return product;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public boolean isNeedsRestock() {
        return needsRestock;
    }

    /**
     * Returns stock percentage relative to reorder level.
     * If reorderLevel is zero or inventory is null, returns 100.0 when quantity>0, otherwise 0.
     */
    public double getStockPercentage() {
        if (inventory == null) return 0.0;
        int qty = inventory.getQuantityInStock();
        int reorder = inventory.getReorderLevel();
        if (reorder <= 0) return qty > 0 ? 100.0 : 0.0;
        return ((double) qty / (double) reorder) * 100.0;
    }
}
