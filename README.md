# Autonomous Lunar Habitat Environmental Control & Resource Reclamation Infrastructure

A Spring Boot (Java 21) backend implementing the spec: habitat module registration,
environmental telemetry ingestion with auto-scrubber adjustment, tenant utility
billing (Sales Order → Customer Invoice, driven by real consumption data),
master data (contacts, products, chart of accounts), double-entry journal
postings, purchase orders / vendor bills, analytic accounts + budgets, and
P&L / Balance Sheet / Revenue-by-Module / Budget reports — plus a browser-based
**mission control frontend** (plain HTML/CSS/JS, no build step) served directly
by Spring Boot, so opening `http://localhost:8080` gives you a working UI for
every module above.

## 1. Prerequisites

- **JDK 21**
- **IntelliJ IDEA** (Community or Ultimate)
- **XAMPP** with **MySQL** and **Apache** (only MySQL is actually needed;
  phpMyAdmin runs on top of Apache)
- Internet access the *first* time you open the project, so Maven can download
  the Spring Boot dependencies

## 2. Start MySQL via XAMPP

1. Open the **XAMPP Control Panel**.
2. Click **Start** next to **MySQL** (and **Apache**, so you can use phpMyAdmin).
3. Go to `http://localhost/phpmyadmin` and create a new database named:
   ```
   lunar_habitat_hub
   ```
   You don't need to create any tables — Hibernate creates/updates them
   automatically on startup (`spring.jpa.hibernate.ddl-auto=update`).
4. If your MySQL root user has a password (default XAMPP has none), update
   `src/main/resources/application.properties`:
   ```
   spring.datasource.username=root
   spring.datasource.password=your_password_here
   ```

## 3. Open the project in IntelliJ

1. **File → Open...** and select the `lunar-habitat-hub` folder (the one
   containing `pom.xml`).
2. IntelliJ will detect it as a Maven project and start downloading
   dependencies automatically. This can take a few minutes the first time.
3. Confirm the Project SDK is Java 21: **File → Project Structure → Project → SDK**.
4. Open `src/main/java/com/lunarhub/LunarHabitatHubApplication.java`.
5. Click the green **Run** arrow next to `main()` (or right-click the file →
   **Run 'LunarHabitatHubApplication'**).
6. Watch the Run console for:
   ```
   Tomcat started on port 8080
   Started LunarHabitatHubApplication in ... seconds
   ```
7. Open **http://localhost:8080** in your browser — that's the mission
   control frontend. It talks to the same-origin REST API, so there's
   nothing extra to configure.

If it fails to connect to MySQL, double-check XAMPP's MySQL service is running
and the port in `application.properties` (`3306`) matches your XAMPP MySQL port.

## 4. Project layout

```
src/main/java/com/lunarhub/
  model/        JPA entities (HabitatModule, TelemetryReading, SalesOrder,
                Invoice, Payment, Contact, Product, Account, Journal,
                JournalEntry, JournalEntryLine, PurchaseOrder, VendorBill,
                AnalyticAccount, Budget)
  repository/   Spring Data JPA repositories for each entity
  service/      Business logic:
                - TelemetryService       ingest readings, auto-adjust scrubbers
                - LifeSupportOrderService module registration, sales order → invoice
                - PaymentService         invoice payment collection
                - AccountingService      double-entry journal postings
                - ReportService          P&L, Balance Sheet, Revenue-by-Module, Budget variance
  controller/   REST controllers exposing the endpoints below
  dto/          Request bodies (CreateModuleRequest, TelemetryRequest,
                CreateSalesOrderRequest, PaymentRequest)
  config/       GlobalExceptionHandler (clean JSON error responses)

src/main/resources/static/    Frontend (served automatically by Spring Boot)
  index.html    Single-page mission control panel: Habitat Modules, Telemetry,
                Orders & Invoices, Master Data, Procurement, Budgets, Reports
  css/style.css Blueprint/schematic theme (Sora + JetBrains Mono, cyan accent)
  js/app.js     All API calls (fetch) and DOM rendering — no framework, no build step
```

## 5. Using the frontend

Once the app is running, go to **http://localhost:8080**:

1. **Habitat Modules** — commission a module with its nominal pressure band
   and water-purity floor; see every module's live status as gauge bars
   (pressure, water purity, oxygen, CO2), colored cyan when nominal and red
   when a gauge is in alert.
2. **Telemetry** — submit a reading for a module. If pressure falls outside
   its band or purity drops below the floor, the system marks it "Adjusted"
   and flips the module to ALERT; otherwise it stays NOMINAL. The
   oxygen/water consumed fields accumulate into that module's utility
   billing.
3. **Orders & Invoices** — issue a monthly life-support service order
   (tenant + module + product + period), then **Convert to invoice**: for
   Reclaimed Oxygen / Potable Water the billed quantity is summed straight
   from telemetry consumption over that period; CO2 Scrubber Servicing and
   Habitat Lease are billed as a flat per-period charge. Collect payment via
   Bank transfer, Cash, or Card to post the ledger entry.
4. **Master Data** — add contacts (tenants/vendors) and products (goods or
   services); the Chart of Accounts table fills in automatically as entries
   get posted.
5. **Procurement** — raise a purchase order (e.g. replacement CO2 filter
   cartridges), convert it to a vendor bill, then pay it via bank.
6. **Budgets** — link an analytic account (facility zone) to a module and
   give it a planned amount for a period.
7. **Reports** — run P&L, Balance Sheet, Revenue-by-Module, and Budget
   Variance on demand.

## 6. Testing the API directly (optional — Postman, curl, or IntelliJ's HTTP client)

### Step 1 — Master data
```
POST http://localhost:8080/api/lunar/modules
{
  "name": "Habitat Dome Alpha",
  "zone": "Shackleton Rim Zone 2",
  "domeAreaM2": 450,
  "pressureMinKPa": 98.0,
  "pressureMaxKPa": 102.0,
  "waterPurityMinPct": 97.0
}

POST http://localhost:8080/api/contacts
{ "name": "Aurora Regolith Mining Co.", "type": "CUSTOMER", "phone": "555-0100" }

POST http://localhost:8080/api/contacts
{ "name": "Selene Sensor Systems", "type": "VENDOR", "phone": "555-0200" }

POST http://localhost:8080/api/products
{ "name": "Reclaimed Oxygen", "category": "GOODS", "unit": "CUBIC_METER", "price": 40.00 }

POST http://localhost:8080/api/products
{ "name": "Potable Water", "category": "GOODS", "unit": "LITER", "price": 2.50 }

POST http://localhost:8080/api/products
{ "name": "CO2 Scrubber Servicing", "category": "SERVICE", "unit": "VISIT", "price": 3000.00 }
```

### Step 2 — Ingest telemetry (tracks consumption, auto-adjusts scrubbers)
```
POST http://localhost:8080/api/lunar/telemetry
{
  "moduleId": 1,
  "atmosphericPressureKPa": 100.2,
  "waterPurityPct": 98.5,
  "oxygenLevelPct": 20.9,
  "co2LevelPpm": 850,
  "oxygenConsumedM3": 12.5,
  "waterConsumedLiters": 340
}
```
If pressure/purity breach the module's thresholds, the response has
`"scrubberAdjusted": true` and the module flips to `ALERT`.

### Step 3 — Monthly service order → invoice
```
POST http://localhost:8080/api/lunar/sales-orders
{
  "tenantId": 1, "moduleId": 1, "productId": 1,
  "periodStart": "2026-09-01", "periodEnd": "2026-09-30"
}

POST http://localhost:8080/api/lunar/sales-orders/1/invoice
```
Sums all `oxygenConsumedM3` telemetry logged for that module in the period,
multiplies by the product's price, and returns an **unpaid Invoice**.

### Step 4 — Collect payment (posts the ledger entry)
```
POST http://localhost:8080/api/payments
{ "invoiceId": 1, "method": "BANK_TRANSFER" }
```

### Step 5 — Procurement (Purchase Order → Vendor Bill → Payment)
```
POST http://localhost:8080/api/purchase/orders
{ "vendor": {"id": 2}, "product": {"id": 3}, "quantity": 1, "unitPrice": 3000.00 }

POST http://localhost:8080/api/purchase/orders/1/bill
PUT  http://localhost:8080/api/purchase/bills/1/pay
```

### Step 6 — Budgets
```
POST http://localhost:8080/api/budget/analytic-accounts
{ "name": "Habitat Dome Alpha", "module": {"id": 1} }

POST http://localhost:8080/api/budget/budgets
{ "analyticAccount": {"id": 1}, "plannedAmount": 50000.00, "periodStart": "2026-09-01", "periodEnd": "2026-09-30" }
```

### Step 7 — Reports
```
GET http://localhost:8080/api/reports/profit-and-loss
GET http://localhost:8080/api/reports/balance-sheet
GET http://localhost:8080/api/reports/revenue-by-module
GET http://localhost:8080/api/reports/budget-variance/1
```

## 7. Full endpoint list

| Method | Path | Purpose |
|---|---|---|
| POST | /api/lunar/modules | Register a habitat module |
| GET  | /api/lunar/modules | List modules |
| POST | /api/lunar/telemetry | Ingest a telemetry reading, auto-adjust scrubbers |
| GET  | /api/lunar/modules/{id}/telemetry | Telemetry history for a module |
| POST | /api/lunar/sales-orders | Issue a monthly service order |
| GET  | /api/lunar/sales-orders | List sales orders |
| POST | /api/lunar/sales-orders/{id}/invoice | Convert consumption into a Customer Invoice |
| POST | /api/contacts | Create customer/vendor |
| GET  | /api/contacts | List contacts |
| POST | /api/products | Create product |
| GET  | /api/products | List products |
| POST | /api/accounts | Create CoA account |
| GET  | /api/accounts | List CoA accounts |
| POST | /api/purchase/orders | Create Purchase Order |
| POST | /api/purchase/orders/{id}/bill | Convert PO → Vendor Bill |
| PUT  | /api/purchase/bills/{id}/pay | Pay vendor bill via bank |
| GET  | /api/invoices | List invoices |
| GET  | /api/invoices/{id} | Get one invoice |
| POST | /api/payments | Collect payment for an invoice |
| POST | /api/budget/analytic-accounts | Create analytic account |
| POST | /api/budget/budgets | Create budget |
| GET  | /api/reports/profit-and-loss | P&L report |
| GET  | /api/reports/balance-sheet | Balance sheet |
| GET  | /api/reports/revenue-by-module | Revenue per habitat module |
| GET  | /api/reports/budget-variance/{analyticAccountId} | Planned vs. actual |

## 8. Notes / things you may want to extend

- Scrubber thresholds live on each `HabitatModule` (`pressureMinKPa`,
  `pressureMaxKPa`, `waterPurityMinPct`) — tune them per module rather than
  globally.
- `AccountingService.ensureAccount/ensureJournal` auto-creates the standard
  Chart of Accounts / Journals the first time they're needed, so you don't
  have to seed them by hand.
- Vendor bills route to different expense/liability account pairs depending
  on the product's category: services (e.g. CO2 Scrubber Servicing) post to
  Scrubber Maintenance Expenditures / Sensor Vendor Creditors, while goods
  (e.g. filter cartridges) post to Supply Vessel Logistics Expenses / Supply
  Vessel Payables.
- There's no authentication/authorization layer yet (Admin vs. Invoicing
  User roles from the spec) — add Spring Security if your assignment
  requires role-based access control.
- The frontend is deliberately framework-free (no React/npm/build step) so
  it runs the moment Spring Boot serves `static/` — if you'd rather build it
  as a React app, the same API endpoints work unchanged.
