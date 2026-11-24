package com.inventory.service;

import com.inventory.config.DatabaseConfig;
import com.inventory.dao.ProductDao;
import com.inventory.dao.SaleDao;
import com.inventory.model.Product;
import com.inventory.model.Sale;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for handling sales transactions and automatically updating inventory.
 */
public class SalesService {
    private final ProductDao productDao;
    private final SaleDao saleDao;

    public SalesService() {
        this.productDao = new ProductDao();
        this.saleDao = new SaleDao();
    }

    public SalesService(ProductDao productDao, SaleDao saleDao) {
        this.productDao = productDao;
        this.saleDao = saleDao;
    }

    /**
     * Step 2 & 3 in Workflow:
     * - Record sale in SQLite
     * - Atomically reduce the product's available quantity
     */
    public Sale recordSale(int productId, LocalDate saleDate, int quantitySold) throws SQLException {
        if (quantitySold <= 0) {
            throw new IllegalArgumentException("Quantity sold must be greater than zero");
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Check product availability
                Optional<Product> productOpt = productDao.findById(productId);
                if (productOpt.isEmpty()) {
                    throw new IllegalArgumentException("Product with ID " + productId + " does not exist");
                }

                Product product = productOpt.get();
                if (product.getQuantityInStock() < quantitySold) {
                    throw new IllegalStateException(String.format(
                            "Insufficient inventory: '%s' has %d in stock, but %d was requested.",
                            product.getName(), product.getQuantityInStock(), quantitySold));
                }

                // 1. Deduct inventory
                boolean stockReduced = productDao.reduceStock(productId, quantitySold, conn);
                if (!stockReduced) {
                    throw new SQLException("Failed to update inventory during sale transaction");
                }

                // 2. Insert sales entry
                Sale sale = new Sale(productId, saleDate, quantitySold, product.getPrice());
                int saleId = saleDao.recordSale(sale, conn);
                sale.setSaleId(saleId);

                // Commit transaction
                conn.commit();
                return sale;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Step 4 in Workflow: Retrieve past sales for ML demand prediction.
     */
    public List<Sale> getHistoricalSales(int productId) throws SQLException {
        return saleDao.findSalesByProduct(productId);
    }

    public List<Sale> getAllSales() throws SQLException {
        return saleDao.findAllSales();
    }
}
