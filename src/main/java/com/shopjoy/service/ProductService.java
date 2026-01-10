package com.shopjoy.service;

import com.shopjoy.dao.CategoryDAO;
import com.shopjoy.dao.InventoryDAO;
import com.shopjoy.dao.OrderItemDAO;
import com.shopjoy.dao.ProductDAO;
import com.shopjoy.model.Inventory;
import com.shopjoy.model.Product;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service layer for Product-related business logic.
 * Sits between controllers and DAOs and enforces validation and higher-level rules.
 */
public class ProductService {

    private final ProductDAO productDAO;
    private final InventoryDAO inventoryDAO;
    private final CategoryDAO categoryDAO;
    private final OrderItemDAO orderItemDAO;

    public ProductService() {
        this.productDAO = new ProductDAO();
        this.inventoryDAO = new InventoryDAO();
        this.categoryDAO = new CategoryDAO();
        this.orderItemDAO = new OrderItemDAO();
    }

    public List<Product> getAllProducts() {
        try {
            return productDAO.findAll();
        } catch (SQLException e) {
            System.err.println("getAllProducts error: " + e.getMessage());
            return null;
        }
    }

    public Product getProductById(int productId) {
        if (productId <= 0) {
            System.err.println("getProductById: invalid id");
            return null;
        }
        try {
            return productDAO.findById(productId);
        } catch (SQLException e) {
            System.err.println("getProductById SQLException: " + e.getMessage());
            return null;
        }
    }

    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            System.err.println("searchProducts: empty keyword");
            return null;
        }
        try {
            return productDAO.searchByName(keyword.trim());
        } catch (SQLException e) {
            System.err.println("searchProducts SQLException: " + e.getMessage());
            return null;
        }
    }

    public List<Product> getProductsByCategory(int categoryId) {
        if (categoryId <= 0) {
            System.err.println("getProductsByCategory: invalid categoryId");
            return null;
        }
        try {
            return productDAO.findByCategory(categoryId);
        } catch (SQLException e) {
            System.err.println("getProductsByCategory SQLException: " + e.getMessage());
            return null;
        }
    }

    public List<Product> getProductsByPriceRange(double min, double max) {
        if (min < 0 || max < min) {
            System.err.println("getProductsByPriceRange: invalid range");
            return null;
        }
        try {
            return productDAO.findByPriceRange(min, max);
        } catch (SQLException e) {
            System.err.println("getProductsByPriceRange SQLException: " + e.getMessage());
            return null;
        }
    }

    public Product addProduct(Product product, int initialStock) {
        if (product == null) {
            System.err.println("addProduct: product is null");
            return null;
        }
        // Validate required fields
        if (!validateProductFields(product)) return null;

        try {
            // Check SKU uniqueness
            Product bySku = productDAO.findBySKU(product.getSku());
            if (bySku != null) {
                System.err.println("addProduct: SKU already exists");
                return null;
            }

            // Check category exists
            if (categoryDAO.findById(product.getCategoryId()) == null) {
                System.err.println("addProduct: category does not exist");
                return null;
            }

            Product saved = productDAO.save(product);
            if (saved == null || saved.getProductId() == 0) {
                System.err.println("addProduct: failed to save product");
                return null;
            }

            // Create inventory record
            Inventory inv = new Inventory();
            inv.setProductId(saved.getProductId());
            inv.setQuantityInStock(Math.max(0, initialStock));
            inv.setReorderLevel(0);
            inv.setWarehouseLocation(null);
            inv.setLastRestocked(LocalDateTime.now());
            inv.setUpdatedAt(LocalDateTime.now());
            inventoryDAO.save(inv);

            return saved;
        } catch (SQLException e) {
            System.err.println("addProduct SQLException: " + e.getMessage());
            return null;
        }
    }

    public boolean updateProduct(Product product) {
        if (product == null || product.getProductId() <= 0) {
            System.err.println("updateProduct: invalid product");
            return false;
        }
        if (!validateProductFields(product)) return false;
        try {
            Product existing = productDAO.findById(product.getProductId());
            if (existing == null) {
                System.err.println("updateProduct: product not found");
                return false;
            }
            return productDAO.update(product) != null;
        } catch (SQLException e) {
            System.err.println("updateProduct SQLException: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteProduct(int productId) {
        if (productId <= 0) return false;
        try {
            // Check if product has existing order items
            List<?> items = orderItemDAO.findByProductId(productId);
            if (items != null && !items.isEmpty()) {
                System.err.println("deleteProduct: product has existing orders; cannot delete");
                return false;
            }

            // Delete inventory first
            Inventory inv = inventoryDAO.findByProductId(productId);
            if (inv != null) inventoryDAO.delete(inv.getInventoryId());

            // Delete product
            return productDAO.delete(productId);
        } catch (SQLException e) {
            System.err.println("deleteProduct SQLException: " + e.getMessage());
            return false;
        }
    }

    public List<Product> getActiveProducts() {
        try {
            return productDAO.findActiveProducts();
        } catch (SQLException e) {
            System.err.println("getActiveProducts SQLException: " + e.getMessage());
            return null;
        }
    }

    public boolean isProductAvailable(int productId, int quantity) {
        if (productId <= 0 || quantity < 0) return false;
        try {
            Inventory inv = inventoryDAO.findByProductId(productId);
            if (inv == null) return false;
            return inv.getQuantityInStock() >= quantity;
        } catch (SQLException e) {
            System.err.println("isProductAvailable SQLException: " + e.getMessage());
            return false;
        }
    }

    public ProductWithInventory getProductWithInventory(int productId) {
        if (productId <= 0) return null;
        try {
            Product p = productDAO.findById(productId);
            if (p == null) return null;
            Inventory inv = inventoryDAO.findByProductId(productId);
            return new ProductWithInventory(p, inv);
        } catch (SQLException e) {
            System.err.println("getProductWithInventory SQLException: " + e.getMessage());
            return null;
        }
    }

    public List<Product> getLowStockProducts() {
        try {
            List<Inventory> low = inventoryDAO.findLowStockItems();
            List<Product> result = new ArrayList<>();
            if (low != null) {
                for (Inventory inv : low) {
                    Product p = productDAO.findById(inv.getProductId());
                    if (p != null) result.add(p);
                }
            }
            return result;
        } catch (SQLException e) {
            System.err.println("getLowStockProducts SQLException: " + e.getMessage());
            return null;
        }
    }

    public boolean updateProductStock(int productId, int newQuantity) {
        if (newQuantity < 0) {
            System.err.println("updateProductStock: newQuantity cannot be negative");
            return false;
        }
        try {
            return inventoryDAO.updateStock(productId, newQuantity);
        } catch (SQLException e) {
            System.err.println("updateProductStock SQLException: " + e.getMessage());
            return false;
        }
    }

    private boolean validateProductFields(Product product) {
        if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
            System.err.println("validateProductFields: product name is required");
            return false;
        }
        if (product.getProductName().length() > 200) {
            System.err.println("validateProductFields: product name too long");
            return false;
        }
        if (product.getPrice() < 0) {
            System.err.println("validateProductFields: price must be >= 0");
            return false;
        }
        if (product.getSku() == null || product.getSku().trim().isEmpty()) {
            System.err.println("validateProductFields: sku is required");
            return false;
        }
        if (product.getCategoryId() <= 0) {
            System.err.println("validateProductFields: categoryId is required");
            return false;
        }
        return true;
    }

    // Inner wrapper class to return product + inventory together
    public static class ProductWithInventory {
        private Product product;
        private Inventory inventory;

        public ProductWithInventory(Product product, Inventory inventory) {
            this.product = product;
            this.inventory = inventory;
        }

        public Product getProduct() { return product; }
        public void setProduct(Product product) { this.product = product; }
        public Inventory getInventory() { return inventory; }
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
    }
}
