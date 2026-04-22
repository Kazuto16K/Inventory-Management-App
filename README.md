# 📦 Inventory Management App

> A full-featured Android inventory management system built with Java and Firebase Firestore, designed for small-to-medium businesses to track stock, manage sales, monitor analytics, and control user access — all in real time.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Firebase Data Model](#firebase-data-model)
- [Screens & Modules](#screens--modules)
- [Setup & Installation](#setup--installation)
- [Permissions](#permissions)
- [Dependencies](#dependencies)
- [Known Limitations](#known-limitations)
- [Screenshots](#screenshots)

---

## Overview

The **Inventory Management App** is an Android application that allows businesses to maintain a complete digital record of their product inventory. It supports two user roles — **Admin** and **Employee** — with role-based access to features like adding/deleting items, viewing audit logs, managing employee accounts, and generating reports.

All data is stored and synced in real time using **Firebase Cloud Firestore**, meaning multiple devices can access and update inventory simultaneously with no local database conflicts.

---

## Features

### 🏠 Dashboard (Homepage)
- Live stats card showing: Total Items, Low Stock count, Out of Stock count, Total Inventory Value
- Full inventory list with color-coded stock status (green / orange / red)
- **Dynamic real-time search** — filters as you type across item name, category, and barcode
- **Barcode / QR code scanner** — scan any product barcode to instantly locate it in inventory
- **Sort options** — sort by Price (Low→High, High→Low), Stock (Low→High, High→Low), or restore original order
- **Category filter** — toggle between All Items and a categorized grid view
- Per-item audit history accessible by tapping any item card

### ➕ Add / Edit Items
- Add new inventory items with name, barcode, category, quantity, price, description, and minimum stock threshold
- Upload a **product image** (stored on Cloudinary, URL saved in Firestore)
- **Barcode scanner** integration for auto-filling the barcode field
- Edit all fields of an existing item; changes are synced to Firebase instantly
- Audit log entry is automatically created on every add/edit action

### 💰 Sell Stock
- Multi-item cart system — select category, pick item, specify quantity, add to cart
- Displays real-time stock and price per item before confirming
- Grand total calculation across all cart items
- Records each sale to Firebase `sales` collection with timestamp, sold-by, customer name
- Sends a **sale confirmation email** to the customer using JavaMail (SMTP)
- Stock quantity is decremented in Firestore upon successful sale

### 📊 Reports & Analytics
- **Line chart** — sales trend over time (daily revenue)
- **Bar chart** — top selling products by units sold
- **Horizontal bar chart** — inventory value breakdown by category
- Toggle between Sales Analytics and Product Analytics views
- Summary cards: Total Revenue, Total Units Sold, Best Performing Category
- Powered by **MPAndroidChart** library

### 🔍 Audit Log
- Full history of all inventory changes (additions, edits, deletions, sales)
- Accessible by Admin only from the Settings screen
- Exportable as a **CSV file** (shareable via any installed app)
- Financial summary table: Current Inventory Value, Total Sales Revenue, Revenue vs Stock Difference

### 👥 User Management (Admin Only)
- View all registered employees
- **Approve** pending employee accounts before they can log in
- Delete employee accounts
- View currently logged-in employees

### ⚙️ Settings
- User profile display (name, email, role)
- **Dark Mode / Light Mode / System Default** theme toggle (persisted across sessions)
- Logout button (clears session and redirects to Login)
- Admin-only section with shortcuts to Audit Logs and User Management

### 🔐 Authentication
- Email + password login (stored in Firestore `users` collection)
- Role assignment at registration (admin / employee)
- Employee accounts require admin approval before first login
- Session persisted locally via `SharedPreferences`

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java (Android SDK) |
| Minimum SDK | API 24 (Android 7.0 Nougat) |
| Target SDK | API 34 (Android 14) |
| Database | Firebase Cloud Firestore |
| Image Storage | Cloudinary |
| Image Loading | Glide 4.16 |
| Charts | MPAndroidChart v3.1 |
| Barcode Scanning | ZXing Android Embedded 4.3.0 |
| Email | JavaMail (android-mail 1.6.2) |
| UI Components | Material Design 3 (Material Components 1.11.0) |
| Build System | Gradle (Kotlin DSL) |

---

## Architecture

The app follows a **layered callback-driven architecture** suited to Firebase's asynchronous nature.

```
┌─────────────────────────────────────────────────────────┐
│                      UI Layer                           │
│   Activities  ──  XML Layouts  ──  RecyclerView         │
│   (DashboardActivity, AddItemActivity, etc.)            │
└──────────────────────┬──────────────────────────────────┘
                       │  calls with callback lambdas
┌──────────────────────▼──────────────────────────────────┐
│                  Data Layer                             │
│              DatabaseHelper.java                        │
│   (all Firebase Firestore queries live here)            │
└──────────────────────┬──────────────────────────────────┘
                       │  Firestore SDK
┌──────────────────────▼──────────────────────────────────┐
│              Firebase Cloud Firestore                   │
│  collections: inventory / users / sales / audit_logs   │
└─────────────────────────────────────────────────────────┘
```

**Key Design Decisions:**

- All Firebase operations are wrapped in `DatabaseHelper.java`. No Activity directly calls the Firestore SDK — this keeps UI code clean and makes the database layer easy to maintain.
- Callbacks are defined as inner interfaces (`ItemListCallback`, `BooleanCallback`, etc.) and passed as lambda arguments, allowing the UI to react when async data arrives.
- `SessionManager.java` wraps `SharedPreferences` to persist login state, username, email, role, and dark mode preference locally on the device.
- Sorting is done in-memory using `Collections.sort()` with comparator lambdas, operating on the already-fetched list without additional Firebase queries.
- Search fetches all documents and filters client-side — this keeps Firestore read costs low for small-to-medium inventories and avoids complex composite indexes.

---

## Firebase Data Model

The app uses **four top-level Firestore collections**:

### `inventory` collection
Each document represents one product. The document ID is auto-generated by Firestore.

| Field | Type | Description |
|---|---|---|
| `id` | String | Firestore document ID (set after creation) |
| `name` | String | Product name |
| `barcode` | String | Barcode / QR code value |
| `category` | String | Product category (e.g. Electronics, Food) |
| `quantity` | Number | Current stock count |
| `price` | Number | Selling price per unit |
| `description` | String | Short product description |
| `minStock` | Number | Low stock threshold |
| `createdAt` | String | Timestamp of creation |
| `imageUrl` | String | Cloudinary hosted image URL |

---

### `users` collection
Document ID is the user's **email address** (used as a natural unique key).

| Field | Type | Description |
|---|---|---|
| `username` | String | Display name |
| `email` | String | Login email |
| `password` | String | Plain-text password *(note: not hashed — see Known Limitations)* |
| `role` | String | `"admin"` or `"employee"` |
| `approved` | Boolean | `true` = can log in; `false` = pending admin approval |
| `isLoggedIn` | Boolean | Tracks active login status |

---

### `sales` collection
Each document is a single sale transaction. Document ID is auto-generated.

| Field | Type | Description |
|---|---|---|
| `item_id` | String | Reference to the sold item's document ID |
| `item_name` | String | Name of item at time of sale |
| `quantity` | Number | Units sold |
| `price_at_sale` | Number | Price per unit at time of sale |
| `customer` | String | Customer name |
| `sold_by` | String | Username/email of the employee who made the sale |
| `timestamp` | String | Date and time of sale |

---

### `audit_logs` collection
Each document records a single inventory change event. Document ID is auto-generated.

| Field | Type | Description |
|---|---|---|
| `item_id` | String | ID of affected item |
| `item_name` | String | Name of affected item |
| `action` | String | e.g. `"ADDED"`, `"EDITED"`, `"DELETED"`, `"SOLD"` |
| `changed_by` | String | Username who performed the action |
| `timestamp` | String | Date and time of the action |
| `details` | String | Human-readable description of the change |

---

## Screens & Modules

### Splash Screen (`SplashActivity`)
The entry point of the app. Displays the app logo briefly, then checks `SessionManager.isLoggedIn()`. If the user is already logged in, it skips to `DashboardActivity`; otherwise it goes to `LoginActivity`.

### Login (`LoginActivity`)
Accepts email and password. Calls `DatabaseHelper.loginUser()` which fetches the Firestore document for that email and compares the password. Also calls `isUserApproved()` to block unapproved employees. On success, calls `DatabaseHelper.getUserRole()` and `getUserName()` then creates a session via `SessionManager.createLoginSession()`.

### Register (`RegisterActivity`)
Collects username, email, password, and role. Checks for duplicate emails via `isEmailExists()`. Admin accounts are auto-approved; employee accounts require admin approval (`approved = false` initially).

### Dashboard (`DashboardActivity`)
The homepage. See the [Use Case Diagram](#3-dashboard--search-sort--scan) for a detailed flow. Key responsibilities:
- Loads and displays all inventory items in a `RecyclerView`
- Manages the search bar with a `TextWatcher` for real-time filtering
- Launches ZXing barcode scanner and passes the result to `filterItems()`
- Sorts `itemList` in-memory via `Collections.sort()` when the Sort popup is used
- Toggles between All Items list and a category grid view

### Add Item (`AddItemActivity`)
A form with fields for all `InventoryItem` properties. Includes a barcode scanner to auto-fill the barcode field and an image picker that uploads to Cloudinary via `CloudinaryHelper`. On save, calls `DatabaseHelper.addItem()` then `insertAuditLog()`.

### Edit Item (`EditItemActivity`)
Pre-fills the same form as AddItem with the selected item's data (passed via `Intent` as a `Serializable` object). On save, calls `DatabaseHelper.updateItem()` and logs the change.

### Sell Stock (`SellStockActivity`)
A two-step screen: category selector → item selector → quantity input → add to cart. Multiple different items can be added to the cart before finalizing. On Finalize, each cart item triggers an `updateItem()` (stock decrement), `insertSale()`, and `insertAuditLog()`. A sale confirmation email is sent via `EmailSender`.

### Reports (`ReportsActivity`)
Fetches all inventory items and sales logs, then aggregates data to render three MPAndroidChart charts. The Toggle Group switches between Sales view (line chart + horizontal bar) and Product view (bar chart). All chart rendering is done client-side from the fetched data.

### Audit Log (`AuditActivity`)
Fetches total inventory value and total revenue from Firestore, computes profit/loss difference, and presents them in a `TableLayout`. An Export button writes a CSV file using `FileOutputStream` and shares it via Android's share intent using `FileProvider`.

### Settings (`SettingsActivity`)
Displays profile info from `SessionManager`. The theme RadioGroup calls `AppCompatDelegate.setDefaultNightMode()` and saves the preference with `SessionManager.setNightMode()`. The admin section (hidden from employees) links to `AuditActivity` and `UserManagementActivity`. Logout calls `SessionManager.logout()`.

---

## Setup & Installation

### Prerequisites
- Android Studio (Hedgehog or newer recommended)
- JDK 8 or higher
- A Firebase project with **Cloud Firestore** enabled
- A **Cloudinary** account for image hosting

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/Kazuto16K/Inventory-Management-App.git
cd Inventory-Management-App
```

**2. Connect Firebase**
- Go to [Firebase Console](https://console.firebase.google.com/) and create a new project
- Add an Android app with package name `com.example.inventoryapp`
- Download the `google-services.json` file
- Place it at `app/google-services.json` (replacing the existing placeholder)
- In the Firebase console, go to **Firestore Database → Create database** (start in test mode for development)

**3. Configure Cloudinary**
- Create a free account at [cloudinary.com](https://cloudinary.com)
- Open `SecretConfig.java` and fill in your Cloud Name, API Key, and API Secret:
```java
public class SecretConfig {
    public static final String CLOUDINARY_CLOUD_NAME = "your_cloud_name";
    public static final String CLOUDINARY_API_KEY    = "your_api_key";
    public static final String CLOUDINARY_API_SECRET = "your_api_secret";
}
```

**4. Configure Email Sender (optional)**
- Open `EmailSender.java` and replace the sender email credentials with a Gmail account that has App Passwords enabled.

**5. Build and run**
- Open the project in Android Studio
- Let Gradle sync complete
- Connect an Android device (API 24+) or start an emulator
- Click **Run ▶**

---

## Permissions

The app declares the following permissions in `AndroidManifest.xml`:

| Permission | Purpose |
|---|---|
| `INTERNET` | Firebase Firestore sync, Cloudinary uploads, email sending |
| `CAMERA` | Barcode / QR code scanning via ZXing |
| `READ_MEDIA_IMAGES` | Picking product images from gallery (API 33+) |
| `WRITE_EXTERNAL_STORAGE` | Exporting audit CSV file (API ≤ 28 only) |
| `READ_EXTERNAL_STORAGE` | Reading files for export (API ≤ 32 only) |

Camera permission is requested at runtime (not just at install) before launching the barcode scanner.

---

## Dependencies

Declared in `app/build.gradle.kts`:

```kotlin
// UI & Layout
implementation("androidx.appcompat:appcompat:1.6.1")
implementation("com.google.android.material:material:1.11.0")
implementation("androidx.constraintlayout:constraintlayout:2.1.4")
implementation("androidx.cardview:cardview:1.0.0")
implementation("androidx.recyclerview:recyclerview:1.3.2")

// Firebase (BOM manages version alignment)
implementation(platform("com.google.firebase:firebase-bom:34.11.0"))
implementation("com.google.firebase:firebase-firestore")
implementation("com.google.firebase:firebase-storage")
implementation("com.google.firebase:firebase-analytics")

// Image Loading
implementation("com.github.bumptech.glide:glide:4.16.0")

// Charts
implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

// Barcode Scanner
implementation("com.journeyapps:zxing-android-embedded:4.3.0")

// Email
implementation("com.sun.mail:android-mail:1.6.2")
implementation("com.sun.mail:android-activation:1.6.2")
```

---

## Known Limitations

- **Passwords are stored as plain text** in Firestore. In a production environment, passwords should be hashed (e.g. using bcrypt) or replaced entirely with Firebase Authentication.
- **Client-side search** — `searchItems()` fetches the entire inventory collection and filters in Java. This is efficient for small inventories (<1000 items) but would need server-side Firestore queries or Algolia integration for larger datasets.
- **No offline support** — the app requires an active internet connection. Firestore offline persistence is not explicitly enabled, so the app will show empty lists if there is no connectivity.
- **Email credentials are hardcoded** in `EmailSender.java`. These should be moved to environment variables or a secure backend endpoint before deploying.
- **Single-currency** — prices are displayed in Indian Rupees (₹) and are not configurable.
- `FirebaseDB.java` is a legacy file left in the project but is not used by any Activity. It can be safely deleted.

---

## Screenshots

> 📸 *Screenshots of the working application will be added here.*

<!-- 
  Suggested screenshots to add:
  1. Splash Screen
  2. Login Screen
  3. Register Screen
  4. Dashboard - Light Mode (with items loaded)
  5. Dashboard - Dark Mode
  6. Dashboard - Search results
  7. Dashboard - Barcode scanner active
  8. Dashboard - Sort options popup
  9. Dashboard - Category view
  10. Add Item form
  11. Edit Item form
  12. Sell Stock screen with cart
  13. Reports - Sales Analytics view
  14. Reports - Product Analytics view
  15. Audit Log screen
  16. Settings screen (Admin view)
  17. User Management screen
-->

| Screen | Screenshot |
|---|---|
| Splash Screen | *(add screenshot)* |
| Login | *(add screenshot)* |
| Register | *(add screenshot)* |
| Dashboard (Light Mode) | *(add screenshot)* |
| Dashboard (Dark Mode) | *(add screenshot)* |
| Search Results | *(add screenshot)* |
| Barcode Scanner | *(add screenshot)* |
| Sort Options | *(add screenshot)* |
| Category View | *(add screenshot)* |
| Add Item | *(add screenshot)* |
| Edit Item | *(add screenshot)* |
| Sell Stock / Cart | *(add screenshot)* |
| Reports — Sales Analytics | *(add screenshot)* |
| Reports — Product Analytics | *(add screenshot)* |
| Audit Log | *(add screenshot)* |
| Settings | *(add screenshot)* |
| User Management | *(add screenshot)* |

---
