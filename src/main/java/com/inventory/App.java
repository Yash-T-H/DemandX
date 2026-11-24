package com.inventory;

import com.inventory.config.DatabaseConfig;
import com.inventory.model.Product;
import com.inventory.service.InventoryService;
import com.inventory.service.SalesService;
import com.inventory.service.StockAlertService;
import com.inventory.ui.ConsoleDashboard;

import java.time.LocalDate;

/**
 * Main application runner for the Inventory Management & Stock-Out Prediction System.
 * Pure Console / CLI-based implementation following the 7-step workflow:
 * 1. Add products
 * 2. Record sales
 * 3. Update inventory
 * 4. Prepare historical data
 * 5. Predict demand (Smile ML)
 * 6. Check stock-out risk
 * 7. Show alerts and reorder suggestions
 */
public class App {

    public static void main(String[] args) {
        System.out.println("Starting Inventory Management & Stock-Out Prediction System...");

        // Initialize SQLite Database schema
        DatabaseConfig.initializeDatabase();

        InventoryService inventoryService = new InventoryService();
        SalesService salesService = new SalesService();
        StockAlertService alertService = new StockAlertService();

        try {
            // Seed sample data if database is currently empty
            if (inventoryService.getAllProducts().isEmpty()) {
                System.out.println("\n[Seeding Initial Demo Catalog & Historical Sales Data...]");
                Product p1 = inventoryService.addProduct("Ergonomic Keyboard", 79.99, 45, 5);  // 5 days lead time
                Product p2 = inventoryService.addProduct("Wireless Mouse", 29.50, 18, 7);     // 7 days lead time (low stock)
                Product p3 = inventoryService.addProduct("USB-C Docking Station", 119.00, 10, 10); // 10 days lead time (high risk)
                Product p4 = inventoryService.addProduct("27-inch 4K Monitor", 299.99, 60, 14); // 14 days lead time (safe)

                LocalDate today = LocalDate.now();

                // Sales for Wireless Mouse (increasing daily demand)
                for (int i = 14; i >= 1; i--) {
                    int qty = 3 + (14 - i) / 4;
                    salesService.recordSale(p2.getProductId(), today.minusDays(i), qty);
                }

                // Sales for USB-C Docking Station (steady demand ~2 units/day)
                for (int i = 14; i >= 1; i--) {
                    salesService.recordSale(p3.getProductId(), today.minusDays(i), 2);
                }

                // Sales for Ergonomic Keyboard (~1 unit every 2 days)
                for (int i = 14; i >= 1; i -= 2) {
                    salesService.recordSale(p1.getProductId(), today.minusDays(i), 2);
                }

                // Sales for Monitor (~1 unit every 3 days)
                for (int i = 12; i >= 1; i -= 3) {
                    salesService.recordSale(p4.getProductId(), today.minusDays(i), 1);
                }
                System.out.println("[Database initialized and seeded with historical records.]");
            }

            // Launch the Interactive Console Dashboard
            ConsoleDashboard dashboard = new ConsoleDashboard(inventoryService, salesService, alertService);
            dashboard.startInteractiveMenu();

        } catch (Exception e) {
            System.err.println("Error executing inventory system: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
