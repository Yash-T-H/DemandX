package com.inventory.service;

import com.inventory.ml.DemandPredictor;
import com.inventory.model.DemandPrediction;
import com.inventory.model.Product;
import com.inventory.model.Sale;
import com.inventory.model.StockAlert;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for evaluating stock-out risks and generating proactive reorder alerts.
 */
public class StockAlertService {
    private final InventoryService inventoryService;
    private final SalesService salesService;
    private final DemandPredictor demandPredictor;

    public StockAlertService(InventoryService inventoryService, SalesService salesService, DemandPredictor demandPredictor) {
        this.inventoryService = inventoryService;
        this.salesService = salesService;
        this.demandPredictor = demandPredictor;
    }

    public StockAlertService() {
        this(new InventoryService(), new SalesService(), new DemandPredictor());
    }

    /**
     * Steps 4, 5, 6, 7 in Workflow:
     * - Retrieve past sales
     * - Predict demand with ML (Smile)
     * - Compare inventory vs supplier delivery time
     * - Generate stock-out risk assessment
     */
    public StockAlert evaluateStockOutRisk(Product product) throws SQLException {
        int leadTimeDays = product.getSupplierLeadTimeDays();
        List<Sale> sales = salesService.getHistoricalSales(product.getProductId());

        // Step 5: ML prediction
        DemandPrediction prediction = demandPredictor.predictDemand(
                product.getProductId(),
                product.getName(),
                sales,
                leadTimeDays
        );

        double dailyDemand = prediction.getPredictedDailyDemand();
        double demandDuringLeadTime = prediction.getTotalExpectedDemand();
        int currentStock = product.getQuantityInStock();

        // Step 6: Risk calculation
        double daysUntilStockout = (dailyDemand > 0.001) ? (currentStock / dailyDemand) : 999.0;

        StockAlert.RiskLevel riskLevel;
        if (currentStock <= demandDuringLeadTime) {
            // Will run out before the new order arrives from supplier!
            riskLevel = StockAlert.RiskLevel.CRITICAL;
        } else if (currentStock <= demandDuringLeadTime * 1.5) {
            // Buffer is thin
            riskLevel = StockAlert.RiskLevel.WARNING;
        } else {
            riskLevel = StockAlert.RiskLevel.SAFE;
        }

        // Recommend reorder quantity: enough for lead time + 14 days safety stock
        int safetyStockDays = 14;
        int recommendedReorder = Math.max(0, (int) Math.ceil((leadTimeDays + safetyStockDays) * dailyDemand) - currentStock);

        return new StockAlert(
                product.getProductId(),
                product.getName(),
                currentStock,
                leadTimeDays,
                demandDuringLeadTime,
                daysUntilStockout,
                riskLevel,
                recommendedReorder
        );
    }

    public List<StockAlert> evaluateAllProducts() throws SQLException {
        List<Product> products = inventoryService.getAllProducts();
        List<StockAlert> alerts = new ArrayList<>();
        for (Product product : products) {
            alerts.add(evaluateStockOutRisk(product));
        }
        return alerts;
    }
}
