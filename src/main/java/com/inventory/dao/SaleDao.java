package com.inventory.dao;

import com.inventory.config.DatabaseConfig;
import com.inventory.model.Sale;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Sales records and historical data preparation.
 */
public class SaleDao {

    public int recordSale(Sale sale, Connection conn) throws SQLException {
        String sql = "INSERT INTO sales (product_id, sale_date, quantity_sold, unit_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, sale.getProductId());
            stmt.setString(2, sale.getSaleDate().toString());
            stmt.setInt(3, sale.getQuantitySold());
            stmt.setDouble(4, sale.getUnitPrice());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    sale.setSaleId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public List<Sale> findSalesByProduct(int productId) throws SQLException {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales WHERE product_id = ? ORDER BY sale_date ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sales.add(new Sale(
                            rs.getInt("sale_id"),
                            rs.getInt("product_id"),
                            LocalDate.parse(rs.getString("sale_date")),
                            rs.getInt("quantity_sold"),
                            rs.getDouble("unit_price")
                    ));
                }
            }
        }
        return sales;
    }

    public List<Sale> findAllSales() throws SQLException {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales ORDER BY sale_date DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                sales.add(new Sale(
                        rs.getInt("sale_id"),
                        rs.getInt("product_id"),
                        LocalDate.parse(rs.getString("sale_date")),
                        rs.getInt("quantity_sold"),
                        rs.getDouble("unit_price")
                ));
            }
        }
        return sales;
    }
}
