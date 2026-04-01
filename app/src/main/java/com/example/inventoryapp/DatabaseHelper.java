package com.example.inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "InventoryDB";
    private static final int DATABASE_VERSION = 1;

    // Users table
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_NAME = "username";
    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_PASSWORD = "password";

    // Inventory table
    public static final String TABLE_INVENTORY = "inventory";
    public static final String COL_ITEM_ID = "id";
    public static final String COL_ITEM_NAME = "name";
    public static final String COL_ITEM_CATEGORY = "category";
    public static final String COL_ITEM_QUANTITY = "quantity";
    public static final String COL_ITEM_PRICE = "price";
    public static final String COL_ITEM_DESCRIPTION = "description";
    public static final String COL_ITEM_MIN_STOCK = "min_stock";
    public static final String COL_ITEM_CREATED_AT = "created_at";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " ("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USER_NAME + " TEXT NOT NULL, "
                + COL_USER_EMAIL + " TEXT UNIQUE NOT NULL, "
                + COL_USER_PASSWORD + " TEXT NOT NULL"
                + ")";

        String createInventoryTable = "CREATE TABLE " + TABLE_INVENTORY + " ("
                + COL_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_ITEM_NAME + " TEXT NOT NULL, "
                + COL_ITEM_CATEGORY + " TEXT, "
                + COL_ITEM_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + COL_ITEM_PRICE + " REAL NOT NULL DEFAULT 0.0, "
                + COL_ITEM_DESCRIPTION + " TEXT, "
                + COL_ITEM_MIN_STOCK + " INTEGER NOT NULL DEFAULT 5, "
                + COL_ITEM_CREATED_AT + " TEXT"
                + ")";

        db.execSQL(createUsersTable);
        db.execSQL(createInventoryTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
        onCreate(db);
    }

    // ===================== USER OPERATIONS =====================

    public boolean registerUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, username);
        values.put(COL_USER_EMAIL, email);
        values.put(COL_USER_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public boolean loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USER_EMAIL + "=? AND " + COL_USER_PASSWORD + "=?",
                new String[]{email, password},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USER_EMAIL + "=?",
                new String[]{email},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public String getUserName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USER_NAME},
                COL_USER_EMAIL + "=?",
                new String[]{email},
                null, null, null);
        String name = "";
        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME));
        }
        cursor.close();
        db.close();
        return name;
    }

    // ===================== INVENTORY OPERATIONS =====================

    public long addItem(InventoryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ITEM_NAME, item.getName());
        values.put(COL_ITEM_CATEGORY, item.getCategory());
        values.put(COL_ITEM_QUANTITY, item.getQuantity());
        values.put(COL_ITEM_PRICE, item.getPrice());
        values.put(COL_ITEM_DESCRIPTION, item.getDescription());
        values.put(COL_ITEM_MIN_STOCK, item.getMinStock());
        values.put(COL_ITEM_CREATED_AT, item.getCreatedAt());
        long id = db.insert(TABLE_INVENTORY, null, values);
        db.close();
        return id;
    }

    public List<InventoryItem> getAllItems() {
        List<InventoryItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_INVENTORY + " ORDER BY " + COL_ITEM_NAME + " ASC", null);
        if (cursor.moveToFirst()) {
            do {
                InventoryItem item = new InventoryItem();
                item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ITEM_ID)));
                item.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_NAME)));
                item.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_CATEGORY)));
                item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ITEM_QUANTITY)));
                item.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_ITEM_PRICE)));
                item.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_DESCRIPTION)));
                item.setMinStock(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ITEM_MIN_STOCK)));
                item.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_CREATED_AT)));
                items.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return items;
    }

    public List<InventoryItem> searchItems(String query) {
        List<InventoryItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_INVENTORY + " WHERE " + COL_ITEM_NAME + " LIKE ? OR " + COL_ITEM_CATEGORY + " LIKE ?",
                new String[]{"%" + query + "%", "%" + query + "%"});
        if (cursor.moveToFirst()) {
            do {
                InventoryItem item = new InventoryItem();
                item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ITEM_ID)));
                item.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_NAME)));
                item.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_CATEGORY)));
                item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ITEM_QUANTITY)));
                item.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_ITEM_PRICE)));
                item.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_DESCRIPTION)));
                item.setMinStock(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ITEM_MIN_STOCK)));
                item.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_CREATED_AT)));
                items.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return items;
    }

    public boolean updateItem(InventoryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ITEM_NAME, item.getName());
        values.put(COL_ITEM_CATEGORY, item.getCategory());
        values.put(COL_ITEM_QUANTITY, item.getQuantity());
        values.put(COL_ITEM_PRICE, item.getPrice());
        values.put(COL_ITEM_DESCRIPTION, item.getDescription());
        values.put(COL_ITEM_MIN_STOCK, item.getMinStock());
        int rows = db.update(TABLE_INVENTORY, values, COL_ITEM_ID + "=?",
                new String[]{String.valueOf(item.getId())});
        db.close();
        return rows > 0;
    }

    public boolean deleteItem(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_INVENTORY, COL_ITEM_ID + "=?",
                new String[]{String.valueOf(itemId)});
        db.close();
        return rows > 0;
    }

    public int getTotalItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_INVENTORY, null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }

    public int getLowStockCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_INVENTORY + " WHERE " + COL_ITEM_QUANTITY + " <= " + COL_ITEM_MIN_STOCK,
                null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }

    public int getOutOfStockCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_INVENTORY + " WHERE " + COL_ITEM_QUANTITY + " = 0",
                null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }

    public double getTotalInventoryValue() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COL_ITEM_QUANTITY + " * " + COL_ITEM_PRICE + ") FROM " + TABLE_INVENTORY,
                null);
        double total = 0.0;
        if (cursor.moveToFirst()) total = cursor.getDouble(0);
        cursor.close();
        db.close();
        return total;
    }
}
