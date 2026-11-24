package com.inventory.model;

/**
 * Model representing a stock-out risk assessment and reorder alert.
 */
public class StockAlert {
    public enum RiskLevel {
        SAFE,      // Stock is plenty for lead time
        WARNING,   // Stock is running low, approaching threshold
        CRITICAL   // Stock will exhaust before or during supplier lead time
    }

    private int productId;
    private String productName;
    private int currentStock;
    private int leadTimeDays;
    private double expectedDemandDuringLeadTime;
    private double daysUntilStockout;
    private RiskLevel riskLevel;
    private int recommendedReorderQuantity;

    public StockAlert(int productId, String productName, int currentStock, int leadTimeDays,
                      double expectedDemandDuringLeadTime, double daysUntilStockout,
                      RiskLevel riskLevel, int recommendedReorderQuantity) {
        this.productId = productId;
        this.productName = productName;
        this.currentStock = currentStock;
        this.leadTimeDays = leadTimeDays;
        this.expectedDemandDuringLeadTime = expectedDemandDuringLeadTime;
        this.daysUntilStockout = daysUntilStockout;
        this.riskLevel = riskLevel;
        this.recommendedReorderQuantity = recommendedReorderQuantity;
    }

    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getCurrentStock() { return currentStock; }
    public int getLeadTimeDays() { return leadTimeDays; }
    public double getExpectedDemandDuringLeadTime() { return expectedDemandDuringLeadTime; }
    public double getDaysUntilStockout() { return daysUntilStockout; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public int getRecommendedReorderQuantity() { return recommendedReorderQuantity; }

    @Override
    public String toString() {
        return String.format("[%s ALERT] %-15s | Stock: %3d | LeadTime: %2dd | Demand in LeadTime: %5.1f | Days Left: %4.1f | Reorder Suggestion: %d units",
                riskLevel, productName, currentStock, leadTimeDays, expectedDemandDuringLeadTime,
                daysUntilStockout, recommendedReorderQuantity);
    }
}
