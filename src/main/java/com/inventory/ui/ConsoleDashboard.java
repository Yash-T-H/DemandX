package com.inventory.ui;

import com.inventory.model.Product;
import com.inventory.model.Sale;
import com.inventory.model.StockAlert;
import com.inventory.service.InventoryService;
import com.inventory.service.SalesService;
import com.inventory.service.StockAlertService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Interactive Console Interface and Dashboard for the Inventory System.
 * Clean, menu-driven CLI ready for future migration to Java Swing.
 */
public class ConsoleDashboard {
    private final InventoryService inventoryService;
    private final SalesService salesService;
    private final StockAlertService alertService;
    private final Scanner scanner;

    public ConsoleDashboard(InventoryService inventoryService, SalesService salesService, StockAlertService alertService) {
        this.inventoryService = inventoryService;
        this.salesService = salesService;
        this.alertService = alertService;
        this.scanner = new Scanner(System.in);
    }

    public void startInteractiveMenu() {
        boolean running = true;
        while (running) {
            printMainMenu();
            System.out.print("Enter your choice [0-6]: ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String input = scanner.nextLine().trim();

            try {
                switch (input) {
                    case "1" -> handleViewInventory();
                    case "2" -> handleAddProduct();
                    case "3" -> handleRecordSale();
                    case "4" -> handleUpdateStock();
                    case "5" -> handleViewSalesHistory();
                    case "6" -> handleRunStockOutAnalysis();
                    case "0" -> {
                        System.out.println("\nExiting Inventory Management System. Goodbye!");
                        running = false;
                    }
                    default -> System.out.println("Invalid option. Please enter a number between 0 and 6.");
                }
            } catch (NumberFormatException nfe) {
                System.out.println("[!] Invalid numeric input. Please enter valid numbers.");
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }

            if (running) {
                System.out.println("\nPress [Enter] to continue...");
                if (scanner.hasNextLine()) {
                    scanner.nextLine();
                } else {
                    break;
                }
            }
        }
    }

    private void printMainMenu() {
        System.out.println("\n" + "=".repeat(68));
        System.out.println("   INVENTORY MANAGEMENT & STOCK-OUT PREDICTION SYSTEM (CLI)");
        System.out.println("=".repeat(68));
        System.out.println("  [1] View Current Inventory Stock");
        System.out.println("  [2] Add New Product");
        System.out.println("  [3] Record a Sale Transaction (Auto-Updates Inventory)");
        System.out.println("  [4] Update Product Stock (Manual Replenishment)");
        System.out.println("  [5] View Sales History Log");
        System.out.println("  [6] Run Demand Forecasting & Stock-Out Alerts (Smile ML)");
        System.out.println("  [0] Exit");
        System.out.println("-".repeat(68));
    }

    public void handleViewInventory() throws Exception {
        List<Product> products = inventoryService.getAllProducts();
        if (products.isEmpty()) {
            System.out.println("\n[!] No products in inventory yet. Use option [2] to add products.");
            return;
        }

        printHeader("CURRENT INVENTORY OVERVIEW");
        System.out.printf("%-5s | %-24s | %-12s | %-10s | %-18s%n",
                "ID", "Product Name", "Price ($)", "In Stock", "Lead Time (Days)");
        System.out.println("-".repeat(78));
        for (Product p : products) {
            System.out.printf("%-5d | %-24s | %12.2f | %10d | %18d%n",
                    p.getProductId(), p.getName(), p.getPrice(), p.getQuantityInStock(), p.getSupplierLeadTimeDays());
        }
        System.out.println("-".repeat(78));
        System.out.printf("Total SKUs: %d%n", products.size());
    }

    private void handleAddProduct() throws Exception {
        printHeader("ADD NEW PRODUCT");
        System.out.print("Enter product name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("[!] Product name cannot be blank.");
            return;
        }

        System.out.print("Enter unit price ($): ");
        double price = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Enter initial quantity in stock: ");
        int stock = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Enter supplier delivery lead time (days) [default 7]: ");
        String leadStr = scanner.nextLine().trim();
        int leadTime = leadStr.isEmpty() ? 7 : Integer.parseInt(leadStr);

        Product created = inventoryService.addProduct(name, price, stock, leadTime);
        System.out.printf("%n[OK] Product successfully added! Generated ID: #%d%n", created.getProductId());
    }

    private void handleRecordSale() throws Exception {
        printHeader("RECORD A SALE TRANSACTION");
        handleViewInventory();

        System.out.print("\nEnter Product ID to sell: ");
        int productId = Integer.parseInt(scanner.nextLine().trim());

        Optional<Product> prodOpt = inventoryService.getProduct(productId);
        if (prodOpt.isEmpty()) {
            System.out.println("[!] Product not found with ID: " + productId);
            return;
        }

        Product p = prodOpt.get();
        System.out.printf("Selected: %s (Available Stock: %d)%n", p.getName(), p.getQuantityInStock());

        System.out.print("Enter quantity sold: ");
        int qty = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Enter sale date (YYYY-MM-DD) [press Enter for Today]: ");
        String dateStr = scanner.nextLine().trim();
        LocalDate saleDate = dateStr.isEmpty() ? LocalDate.now() : LocalDate.parse(dateStr);

        Sale sale = salesService.recordSale(productId, saleDate, qty);
        System.out.printf("%n[OK] Sale recorded (Sale ID: #%d). Inventory automatically updated for '%s'!%n",
                sale.getSaleId(), p.getName());
    }

    private void handleUpdateStock() throws Exception {
        printHeader("UPDATE PRODUCT INVENTORY");
        handleViewInventory();

        System.out.print("\nEnter Product ID to update: ");
        int productId = Integer.parseInt(scanner.nextLine().trim());

        Optional<Product> prodOpt = inventoryService.getProduct(productId);
        if (prodOpt.isEmpty()) {
            System.out.println("[!] Product not found with ID: " + productId);
            return;
        }

        Product p = prodOpt.get();
        System.out.printf("Current stock for '%s' is %d.%n", p.getName(), p.getQuantityInStock());
        System.out.print("Enter new total stock quantity: ");
        int newStock = Integer.parseInt(scanner.nextLine().trim());

        boolean updated = inventoryService.updateStock(productId, newStock);
        if (updated) {
            System.out.printf("[OK] Stock for '%s' updated to %d units.%n", p.getName(), newStock);
        } else {
            System.out.println("[!] Failed to update stock.");
        }
    }

    private void handleViewSalesHistory() throws Exception {
        printHeader("SALES TRANSACTION HISTORY LOG");
        List<Sale> sales = salesService.getAllSales();
        if (sales.isEmpty()) {
            System.out.println("\n[!] No sales transactions recorded yet.");
            return;
        }

        System.out.printf("%-8s | %-12s | %-12s | %-12s | %-12s%n",
                "Sale ID", "Product ID", "Sale Date", "Qty Sold", "Unit Price");
        System.out.println("-".repeat(66));
        for (Sale s : sales) {
            System.out.printf("%-8d | %-12d | %-12s | %12d | %12.2f%n",
                    s.getSaleId(), s.getProductId(), s.getSaleDate(), s.getQuantitySold(), s.getUnitPrice());
        }
        System.out.println("-".repeat(66));
        System.out.printf("Total Transactions: %d%n", sales.size());
    }

    public void handleRunStockOutAnalysis() throws Exception {
        printHeader("AI DEMAND FORECASTING & STOCK-OUT RISK ASSESSMENT (SMILE ML)");
        List<StockAlert> alerts = alertService.evaluateAllProducts();

        if (alerts.isEmpty()) {
            System.out.println("\n[!] No products in catalog to evaluate.");
            return;
        }

        System.out.printf("%-10s | %-22s | %-7s | %-11s | %-15s | %-12s | %-14s%n",
                "STATUS", "Product Name", "Stock", "Lead Time", "Demand in Window", "Days Left", "Reorder Qty");
        System.out.println("-".repeat(100));

        int criticalCount = 0;
        int warningCount = 0;

        for (StockAlert a : alerts) {
            String badge = switch (a.getRiskLevel()) {
                case CRITICAL -> {
                    criticalCount++;
                    yield "[CRITICAL]";
                }
                case WARNING -> {
                    warningCount++;
                    yield "[WARNING] ";
                }
                case SAFE -> "[OK/SAFE] ";
            };

            System.out.printf("%-10s | %-22s | %7d | %9d d | %15.1f | %10.1f d | %14d units%n",
                    badge, a.getProductName(), a.getCurrentStock(), a.getLeadTimeDays(),
                    a.getExpectedDemandDuringLeadTime(), a.getDaysUntilStockout(), a.getRecommendedReorderQuantity());
        }
        System.out.println("-".repeat(100));
        System.out.printf("Summary: %d CRITICAL stock-outs | %d WARNINGS | %d SAFE%n",
                criticalCount, warningCount, (alerts.size() - criticalCount - warningCount));
        System.out.println("Note: Predictions are generated via Smile ML regression on historical transaction intervals.");
    }

    public static void printHeader(String title) {
        System.out.println("\n" + "=".repeat(78));
        System.out.println("  " + title);
        System.out.println("=".repeat(78));
    }
}
