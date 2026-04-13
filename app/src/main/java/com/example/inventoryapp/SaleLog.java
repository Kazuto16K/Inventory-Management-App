package com.example.inventoryapp;

import com.google.firebase.firestore.PropertyName;

public class SaleLog {
    private String itemId;
    private String itemName;
    private int quantity;
    private double priceAtSale;
    private String customer;
    private String soldBy;
    private String timestamp;

    public SaleLog() {}

    @PropertyName("item_id")
    public String getItemId() { return itemId; }
    @PropertyName("item_id")
    public void setItemId(String itemId) { this.itemId = itemId; }

    @PropertyName("item_name")
    public String getItemName() { return itemName; }
    @PropertyName("item_name")
    public void setItemName(String itemName) { this.itemName = itemName; }

    @PropertyName("quantity")
    public int getQuantity() { return quantity; }
    @PropertyName("quantity")
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @PropertyName("price_at_sale")
    public double getPriceAtSale() { return priceAtSale; }
    @PropertyName("price_at_sale")
    public void setPriceAtSale(double priceAtSale) { this.priceAtSale = priceAtSale; }

    @PropertyName("customer")
    public String getCustomer() { return customer; }
    @PropertyName("customer")
    public void setCustomer(String customer) { this.customer = customer; }

    @PropertyName("sold_by")
    public String getSoldBy() { return soldBy; }
    @PropertyName("sold_by")
    public void setSoldBy(String soldBy) { this.soldBy = soldBy; }

    @PropertyName("timestamp")
    public String getTimestamp() { return timestamp; }
    @PropertyName("timestamp")
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
