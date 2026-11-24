package com.inventory.model;

/**
 * Domain entity representing a product in inventory.
 */
public class Product {
    private int productId;
    private String name;
    private double price;
    private int quantityInStock;
    private int supplierLeadTimeDays; // Delivery time in days from supplier

    public Product() {}

    public Product(int productId, String name, double price, int quantityInStock, int supplierLeadTimeDays) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantityInStock = quantityInStock;
        this.supplierLeadTimeDays = supplierLeadTimeDays;
    }

    public Product(String name, double price, int quantityInStock, int supplierLeadTimeDays) {
        this(-1, name, price, quantityInStock, supplierLeadTimeDays);
    }

    public Product(String name, double price, int quantityInStock) {
        this(-1, name, price, quantityInStock, 7); // Default 7 days lead time
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(int quantityInStock) {
        this.quantityInStock = quantityInStock;
    }

    public int getSupplierLeadTimeDays() {
        return supplierLeadTimeDays;
    }

    public void setSupplierLeadTimeDays(int supplierLeadTimeDays) {
        this.supplierLeadTimeDays = supplierLeadTimeDays;
    }

    @Override
    public String toString() {
        return String.format("Product [ID: %d | Name: %-15s | Price: $%.2f | Stock: %4d | Lead Time: %2d days]",
                productId, name, price, quantityInStock, supplierLeadTimeDays);
    }
}
