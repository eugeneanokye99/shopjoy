package com.shopjoy.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Product {
    private int productId;
    private String productName;
    private String description;
    private int categoryId;
    private double price;
    private double costPrice;
    private String sku;
    private String brand;
    private String imageUrl;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Product() {}

    public Product(String productName, String description, int categoryId, double price, double costPrice, String sku, String brand, String imageUrl, boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        setProductName(productName);
        this.description = description;
        this.categoryId = categoryId;
        setPrice(price);
        setCostPrice(costPrice);
        this.sku = sku;
        this.brand = brand;
        this.imageUrl = imageUrl;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Product(int productId, String productName, String description, int categoryId, double price, double costPrice, String sku, String brand, String imageUrl, boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.productId = productId;
        setProductName(productName);
        this.description = description;
        this.categoryId = categoryId;
        setPrice(price);
        setCostPrice(costPrice);
        this.sku = sku;
        this.brand = brand;
        this.imageUrl = imageUrl;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) {
        if (productName == null || productName.trim().isEmpty()) throw new IllegalArgumentException("productName cannot be null or empty");
        this.productName = productName;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public double getPrice() { return price; }
    public void setPrice(double price) {
        if (price < 0) throw new IllegalArgumentException("price must be >= 0");
        this.price = price;
    }

    public double getCostPrice() { return costPrice; }
    public void setCostPrice(double costPrice) {
        if (costPrice < 0) throw new IllegalArgumentException("costPrice must be >= 0");
        this.costPrice = costPrice;
    }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", description='" + description + '\'' +
                ", categoryId=" + categoryId +
                ", price=" + price +
                ", costPrice=" + costPrice +
                ", sku='" + sku + '\'' +
                ", brand='" + brand + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return productId == product.productId;
    }

    @Override
    public int hashCode() { return Objects.hash(productId); }
}
