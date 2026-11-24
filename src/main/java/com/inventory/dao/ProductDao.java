package com.inventory.dao;

import com.inventory.config.DatabaseConfig;
import com.inventory.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Product entities.
 */
public class ProductDao {

    public int addProduct(Product product) throws SQLException {
        String sql = "INSERT INTO products (name, price, quantity_in_stock, supplier_lead_time_days) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, product.getName());
            stmt.setDouble(2, product.getPrice());
            stmt.setInt(3, product.getQuantityInStock());
            stmt.setInt(4, product.getSupplierLeadTimeDays());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    product.setProductId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public Optional<Product> findById(int productId) throws SQLException {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Product> findAll() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY product_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    public boolean updateStock(int productId, int newQuantity) throws SQLException {
        String sql = "UPDATE products SET quantity_in_stock = ? WHERE product_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean reduceStock(int productId, int quantitySold, Connection conn) throws SQLException {
        String sql = "UPDATE products SET quantity_in_stock = quantity_in_stock - ? WHERE product_id = ? AND quantity_in_stock >= ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantitySold);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantitySold);
            return stmt.executeUpdate() > 0;
        }
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("product_id"),
                rs.getString("name"),
                rs.getDouble("price"),
                rs.getInt("quantity_in_stock"),
                rs.getInt("supplier_lead_time_days")
        );
    }
}
