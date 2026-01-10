package com.shopjoy.service;

import com.shopjoy.dao.CartItemDAO;
import com.shopjoy.dao.ProductDAO;
import com.shopjoy.model.CartItem;
import com.shopjoy.model.Order;
import com.shopjoy.model.OrderItem;
import com.shopjoy.model.Product;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CartService {

    private final CartItemDAO cartItemDAO;
    private final ProductDAO productDAO;
    private final OrderService orderService;
    private final InventoryService inventoryService;

    public CartService() {
        this.cartItemDAO = new CartItemDAO();
        this.productDAO = new ProductDAO();
        this.orderService = new OrderService();
        this.inventoryService = new InventoryService();

        // Ensure table exists
        this.cartItemDAO.createTableIfNotExists();
    }

    public List<CartItem> getCartItems(int userId) {
        try {
            List<CartItem> items = cartItemDAO.findByUserId(userId);
            // Enrich with product details
            for (CartItem item : items) {
                Product p = productDAO.findById(item.getProductId());
                item.setProduct(p);
            }
            return items;
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public boolean addToCart(int userId, int productId, int quantity) {
        try {
            // Check current quantity in cart for this user and product
            CartItem existing = cartItemDAO.findByUserAndProduct(userId, productId);
            int totalInCart = (existing != null ? existing.getQuantity() : 0) + quantity;

            // Check stock availability for the total requested quantity
            if (!inventoryService.isStockAvailable(productId, totalInCart)) {
                System.err.println("addToCart: Insufficient stock for product " + productId + " (Requested total: "
                        + totalInCart + ")");
                return false;
            }

            CartItem item = new CartItem(userId, productId, quantity);
            cartItemDAO.save(item);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateQuantity(int cartItemId, int newQuantity) {
        if (newQuantity <= 0) {
            return removeFromCart(cartItemId);
        }
        try {
            CartItem item = cartItemDAO.findById(cartItemId);
            if (item != null) {
                // Check stock availability for the new quantity
                if (!inventoryService.isStockAvailable(item.getProductId(), newQuantity)) {
                    System.err.println("updateQuantity: Insufficient stock for product " + item.getProductId());
                    return false;
                }
                item.setQuantity(newQuantity);
                cartItemDAO.update(item);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeFromCart(int cartItemId) {
        try {
            return cartItemDAO.delete(cartItemId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean clearCart(int userId) {
        try {
            cartItemDAO.clearCart(userId);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Order checkout(int userId, String shippingAddress, String paymentMethod) {
        List<CartItem> cartItems = getCartItems(userId);
        if (cartItems.isEmpty())
            return null;

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem ci : cartItems) {
            OrderItem oi = new OrderItem();
            oi.setProductId(ci.getProductId());
            oi.setQuantity(ci.getQuantity());
            // Unit price is set by OrderService logic
            orderItems.add(oi);
        }

        Order order = orderService.createOrder(userId, orderItems, shippingAddress, paymentMethod);
        if (order != null) {
            clearCart(userId);
        }
        return order;
    }
}
