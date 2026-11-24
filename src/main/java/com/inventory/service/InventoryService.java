package com.inventory.service;

import com.inventory.dao.ProductDao;
import com.inventory.model.Product;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service for handling Product catalog and inventory replenishment.
 */
public class InventoryService {
    private final ProductDao productDao;

    public InventoryService() {
        this.productDao = new ProductDao();
    }

    public InventoryService(ProductDao productDao) {
        this.productDao = productDao;
    }

    /**
     * Step 1 in Workflow: Add product to catalog and inventory.
     */
    public Product addProduct(String name, double price, int initialStock, int leadTimeDays) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (initialStock < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        Product product = new Product(name.trim(), price, initialStock, leadTimeDays);
        int generatedId = productDao.addProduct(product);
        product.setProductId(generatedId);
        return product;
    }

    /**
     * Step 3 in Workflow: Update inventory stock directly.
     */
    public boolean updateStock(int productId, int newQuantity) throws SQLException {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        return productDao.updateStock(productId, newQuantity);
    }

    public Optional<Product> getProduct(int productId) throws SQLException {
        return productDao.findById(productId);
    }

    public List<Product> getAllProducts() throws SQLException {
        return productDao.findAll();
    }
}
