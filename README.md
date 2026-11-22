# DemandX

An end-to-end Java and Machine Learning application designed to manage inventory, track historical sales transactions, predict future product demand using **Smile ML**, and proactively trigger stock-out risk alerts based on supplier delivery lead times.

> **Zero Setup Required**: The repository includes the SQLite JDBC driver directly in `lib/` and execution launcher scripts (`run.bat` for Windows and `run.sh` for Linux/Mac). Anyone downloading this repository can run it immediately with just Java installed!

---

## 🚀 How to Run (Single Command)

### On Windows:
Double-click `run.bat` or run in PowerShell / Command Prompt:
```powershell
.\run.bat
```
*(Or manually: `java -cp "src\main\java;lib\*" com.inventory.App`)*

### On Mac / Linux:
```bash
chmod +x run.sh
./run.sh
```

---

## 🖥️ Interactive Console Menu

When launched, the system automatically initializes the SQLite database (`inventory.db`), seeds initial demo catalog & sales records, and opens the interactive menu:

```text
====================================================================
   INVENTORY MANAGEMENT & STOCK-OUT PREDICTION SYSTEM (CLI)
====================================================================
  [1] View Current Inventory Stock
  [2] Add New Product
  [3] Record a Sale Transaction (Auto-Updates Inventory)
  [4] Update Product Stock (Manual Replenishment)
  [5] View Sales History Log
  [6] Run Demand Forecasting & Stock-Out Alerts (Smile ML)
  [0] Exit
--------------------------------------------------------------------
```

---

## 🛠 Tech Stack

| Technology | Role |
| :--- | :--- |
| **Java 17+ / 21+** | Core programming language, business services, domain models |
| **SQLite** | Embedded relational database for zero-config persistence (`inventory.db`) |
| **JDBC** | Database connectivity & transactional integrity (driver included in `lib/`) |
| **Smile ML** | Statistical Machine Intelligence and Learning Engine (Regression/Demand Forecasting) |
| **Console UI** | Interactive, menu-driven CLI terminal dashboard |

---

## 🔄 Workflow Implementation

The application strictly implements the 7-step lifecycle defined in `inventory_project_tech_stack_workflow.txt`:

1. **Add Products**: Product catalog management storing `product_id`, `name`, `price`, `quantity_in_stock`, and `supplier_lead_time_days`.
2. **Record Sales**: Captures every sales transaction with timestamp, product ID, and quantity sold.
3. **Update Inventory**: Transactional deduction of inventory stock when sales occur (`setAutoCommit(false)` + rollback on failure).
4. **Prepare Historical Data**: Queries sales history and aggregates daily sales totals chronologically for feature extraction.
5. **Predict Demand**: Trains an Ordinary Least Squares (OLS) / trend regression model via **Smile ML** to compute expected daily consumption rates.
6. **Check Stock-Out Risk**: Compares current on-hand inventory with projected demand during the supplier's replenishment window ($\text{Days Until Stockout} = \frac{\text{Current Stock}}{\text{Daily Demand}}$).
7. **Show Alerts**: Emits risk categorizations (`SAFE`, `WARNING`, `CRITICAL`) along with recommended reorder batch sizes.

---

## 📂 Project Structure

```
d:\java project\
│   inventory_project_tech_stack_workflow.txt
│   pom.xml                                       # Maven configuration
│   README.md                                     # Documentation
│   run.bat                                       # 1-Click launcher for Windows
│   run.sh                                        # 1-Click launcher for Mac/Linux
│
├───lib
│       sqlite-jdbc.jar                           # Included self-contained SQLite driver
│
├───src
│   └───main
│       ├───java
│       │   └───com
│       │       └───inventory
│       │           │   App.java                  # Main entry point & CLI menu loop
│       │           │
│       │           ├───config
│       │           │       DatabaseConfig.java   # SQLite connection & table initialization
│       │           │
│       │           ├───dao
│       │           │       ProductDao.java       # Product CRUD & stock operations
│       │           │       SaleDao.java          # Sales recording & time-series retrieval
│       │           │
│       │           ├───model
│       │           │       Product.java          # Product domain entity
│       │           │       Sale.java             # Sales transaction entity
│       │           │       DemandPrediction.java # Machine learning output model
│       │           │       StockAlert.java       # Stock-out risk assessment
│       │           │
│       │           ├───ml
│       │           │       DemandPredictor.java  # Smile ML regression & trend forecasting
│       │           │
│       │           ├───service
│       │           │       InventoryService.java # Catalog and inventory business logic
│       │           │       SalesService.java     # Atomic stock deduction & sales recording
│       │           │       StockAlertService.java# Lead-time vs demand risk evaluation
│       │           │
│       │           └───ui
│       │                   ConsoleDashboard.java # Interactive CLI menu & ASCII tables
│       │
│       └───resources
│               db-schema.sql                     # SQLite tables & indexes
```


💀
