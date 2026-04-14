package com.example.inventoryapp;

import java.io.Serializable;

public class InventoryTransaction implements Serializable {
    private String id;
    private String itemId;
    private String itemName;
    private String type; // "INITIAL", "ADD", "REDUCE", "SALE", "DELETE"
    private int quantity;
    private double price; // Price per unit at the time of transaction
    private String reason;
    private String timestamp;
    private String user;

    public InventoryTransaction() {}

    public InventoryTransaction(String itemId, String itemName, String type, int quantity, double price, String reason, String timestamp, String user) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.reason = reason;
        this.timestamp = timestamp;
        this.user = user;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
}
