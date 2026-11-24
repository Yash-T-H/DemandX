package com.inventory.ml;

import com.inventory.model.DemandPrediction;
import com.inventory.model.Sale;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Machine Learning Engine for Product Demand Prediction.
 * Utilizes Smile regression / linear trend modeling on historical sales data.
 */
public class DemandPredictor {

    /**
     * Predicts daily and horizon demand for a product given historical sales.
     *
     * @param productId product identifier
     * @param productName human-readable name
     * @param sales historical sales entries
     * @param forecastDays number of days to project into the future (typically supplier lead time)
     * @return DemandPrediction with expected daily demand and total projected demand
     */
    public DemandPrediction predictDemand(int productId, String productName, List<Sale> sales, int forecastDays) {
        if (sales == null || sales.isEmpty()) {
            return new DemandPrediction(productId, productName, 0.0, forecastDays, "No Data (0 units)");
        }

        // Aggregate sales by date (to get total quantity sold per calendar day)
        Map<LocalDate, Double> dailyTotals = new TreeMap<>();
        for (Sale sale : sales) {
            dailyTotals.merge(sale.getSaleDate(), (double) sale.getQuantitySold(), Double::sum);
        }

        if (dailyTotals.size() == 1) {
            double singleDayQty = dailyTotals.values().iterator().next();
            return new DemandPrediction(productId, productName, singleDayQty, forecastDays, "Single-Point Baseline");
        }

        // Prepare training data: X = time elapsed in days from first sale, Y = daily quantity sold
        LocalDate startDate = dailyTotals.keySet().iterator().next();
        int n = dailyTotals.size();
        double[][] x = new double[n][1];
        double[] y = new double[n];

        int idx = 0;
        for (Map.Entry<LocalDate, Double> entry : dailyTotals.entrySet()) {
            long daysFromStart = ChronoUnit.DAYS.between(startDate, entry.getKey());
            x[idx][0] = (double) daysFromStart;
            y[idx] = entry.getValue();
            idx++;
        }

        double predictedDailyDemand = 0.0;
        String methodUsed = "Smile ML (OLS Regression)";

        try {
            // Attempt Smile ML OLS Regression via reflection or Smile API
            // Smile 3.x / 2.x standard call: smile.regression.OLS.fit(x, y)
            Class<?> olsClass = Class.forName("smile.regression.OLS");
            java.lang.reflect.Method fitMethod = olsClass.getMethod("fit", double[][].class, double[].class);
            Object model = fitMethod.invoke(null, (Object) x, (Object) y);

            // Predict demand for future day index (average day in the forecast horizon)
            double targetDay = x[n - 1][0] + (forecastDays / 2.0);
            java.lang.reflect.Method predictMethod = model.getClass().getMethod("predict", double[].class);
            Object predictionObj = predictMethod.invoke(model, (Object) new double[]{targetDay});

            if (predictionObj instanceof Number) {
                predictedDailyDemand = ((Number) predictionObj).doubleValue();
            }
        } catch (Throwable t) {
            // Robust internal Regression / Exponentially Weighted Moving Average baseline
            methodUsed = "Weighted Linear Regression (Built-in)";
            predictedDailyDemand = calculateLinearRegressionPrediction(x, y, x[n - 1][0] + (forecastDays / 2.0));
        }

        // Fallback or lower bound guard (demand cannot be negative)
        if (predictedDailyDemand <= 0.0) {
            double average = Arrays.stream(y).average().orElse(0.0);
            predictedDailyDemand = Math.max(0.1, average);
            methodUsed += " + Mean Fallback";
        }

        return new DemandPrediction(productId, productName, predictedDailyDemand, forecastDays, methodUsed);
    }

    /**
     * Fallback standard Ordinary Least Squares (OLS) calculation: y = alpha + beta * x
     */
    private double calculateLinearRegressionPrediction(double[][] x, double[] y, double targetX) {
        int n = y.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            double xi = x[i][0];
            double yi = y[i];
            sumX += xi;
            sumY += yi;
            sumXY += xi * yi;
            sumX2 += xi * xi;
        }

        double denominator = (n * sumX2 - sumX * sumX);
        if (Math.abs(denominator) < 1e-9) {
            return sumY / n;
        }

        double slope = (n * sumXY - sumX * sumY) / denominator;
        double intercept = (sumY - slope * sumX) / n;

        return intercept + slope * targetX;
    }
}
