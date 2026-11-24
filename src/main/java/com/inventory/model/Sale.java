package com.inventory.model;

import java.time.LocalDate;

/**
 * Domain entity representing an individual sales transaction.
 */
public class Sale {
    private int saleId;
    private int productId;
    private LocalDate saleDate;
    private int quantitySold;
    private double unitPrice;

    public Sale() {}

    public Sale(int saleId, int productId, LocalDate saleDate, int quantitySold, double unitPrice) {
        this.saleId = saleId;
        this.productId = productId;
        this.saleDate = saleDate;
        this.quantitySold = quantitySold;
        this.unitPrice = unitPrice;
    }

    public Sale(int productId, LocalDate saleDate, int quantitySold, double unitPrice) {
        this(-1, productId, saleDate, quantitySold, unitPrice);
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
    }

    public int getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(int quantitySold) {
        this.quantitySold = quantitySold;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    @Override
    public String toString() {
        return String.format("Sale [ID: %d | Product ID: %d | Date: %s | Qty Sold: %d | Unit Price: $%.2f]",
                saleId, productId, saleDate, quantitySold, unitPrice);
    }
}
