// ===== Small helpers =====
const API = ""; // same-origin: served by Spring Boot alongside the static files

async function api(path, method = "GET", body) {
  const res = await fetch(API + path, {
    method,
    headers: body ? { "Content-Type": "application/json" } : undefined,
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) {
    let message = res.statusText;
    try {
      const data = await res.json();
      message = data.error || message;
    } catch (_) { /* no JSON body */ }
    throw new Error(message);
  }
  if (res.status === 204) return null;
  return res.json();
}

function money(v) {
  const n = Number(v || 0);
  return "₹" + n.toFixed(2);
}

function shortDate(iso) {
  if (!iso) return "—";
  const d = new Date(iso);
  return d.toLocaleDateString(undefined, { month: "short", day: "numeric" }) +
         " " + d.toLocaleTimeString(undefined, { hour: "2-digit", minute: "2-digit" });
}

let toastTimer;
function toast(message, isError = false) {
  const el = document.getElementById("toast");
  el.textContent = message;
  el.classList.toggle("is-error", isError);
  el.classList.add("is-visible");
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => el.classList.remove("is-visible"), 3200);
}

function option(value, label) {
  const o = document.createElement("option");
  o.value = value;
  o.textContent = label;
  return o;
}

function clamp01(v) { return Math.max(0, Math.min(1, v)); }

// ===== Navigation =====
function initTabs() {
  const tabs = document.querySelectorAll(".tab");
  tabs.forEach(tab => {
    tab.addEventListener("click", () => {
      tabs.forEach(t => t.classList.remove("is-active"));
      tab.classList.add("is-active");
      document.querySelectorAll(".panel-section").forEach(s => s.classList.remove("is-active"));
      document.getElementById("section-" + tab.dataset.section).classList.add("is-active");
      refreshSection(tab.dataset.section);
    });
  });
}

function refreshSection(name) {
  if (name === "modules") { loadModules(); }
  if (name === "telemetry") { loadModulesForTelemetry(); }
  if (name === "orders") { loadTenantsForOrder(); loadModulesForOrder(); loadProductsForOrder(); loadSalesOrders(); loadInvoices(); }
  if (name === "master") { loadContacts(); loadProducts(); loadAccounts(); }
  if (name === "purchases") { loadVendorsAndProducts(); loadPurchaseOrders(); loadBills(); }
  if (name === "budgets") { loadModulesForAnalytic(); loadAnalyticAccounts(); loadBudgets(); }
  if (name === "reports") { loadAnalyticForVariance(); }
}

// ===== Clock + API status =====
function tickClock() {
  document.getElementById("clock").textContent = new Date().toLocaleTimeString();
}

async function checkApiStatus() {
  const chip = document.getElementById("apiChip");
  try {
    await api("/api/lunar/modules");
    chip.textContent = "API link nominal";
    chip.className = "api-chip ok";
  } catch (e) {
    chip.textContent = "API link down";
    chip.className = "api-chip down";
  }
}

// ===== Habitat modules (dashboard) =====
async function loadModules() {
  const container = document.getElementById("moduleCards");
  try {
    const modules = await api("/api/lunar/modules");
    updateTally(modules);
    if (!modules.length) {
      container.innerHTML = '<p class="empty">No modules commissioned yet.</p>';
      return;
    }
    container.innerHTML = "";
    for (const m of modules) {
      let latest = null;
      try {
        const history = await api(`/api/lunar/modules/${m.id}/telemetry`);
        latest = history[0] || null;
      } catch (_) { /* ignore */ }
      container.appendChild(buildModuleCard(m, latest));
    }
  } catch (e) { toast("Could not load modules: " + e.message, true); }
}

function updateTally(modules) {
  const nominal = modules.filter(m => m.status === "NOMINAL").length;
  const alert = modules.filter(m => m.status === "ALERT").length;
  document.getElementById("tallyNominal").textContent = `${nominal} nominal`;
  document.getElementById("tallyAlert").textContent = `${alert} alert`;
}

function buildModuleCard(m, latest) {
  const div = document.createElement("div");
  div.className = "module-card";
  const isAlert = m.status === "ALERT";

  const pressure = latest ? Number(latest.atmosphericPressureKPa) : null;
  const purity = latest ? Number(latest.waterPurityPct) : null;
  const oxygen = latest ? Number(latest.oxygenLevelPct) : null;
  const co2 = latest ? Number(latest.co2LevelPpm) : null;

  const pMin = Number(m.pressureMinKPa), pMax = Number(m.pressureMaxKPa);
  const pressurePct = pressure === null ? 0 : clamp01((pressure - pMin) / (pMax - pMin || 1));
  const pressureAlert = pressure !== null && (pressure < pMin || pressure > pMax);
  const purityAlert = purity !== null && purity < Number(m.waterPurityMinPct);

  div.innerHTML = `
    <div class="module-card-head">
      <div>
        <div class="name">${m.name}</div>
        <div class="zone">${m.zone}</div>
      </div>
      <span class="chip ${isAlert ? "alert" : "nominal"}">${m.status}</span>
    </div>
    ${gaugeRow("Pressure", pressure === null ? "—" : pressure.toFixed(1) + " kPa", pressurePct, pressureAlert)}
    ${gaugeRow("Water purity", purity === null ? "—" : purity.toFixed(1) + "%", purity === null ? 0 : clamp01(purity / 100), purityAlert)}
    ${gaugeRow("Oxygen", oxygen === null ? "—" : oxygen.toFixed(1) + "%", oxygen === null ? 0 : clamp01(oxygen / 100), false)}
    ${gaugeRow("CO2", co2 === null ? "—" : co2.toFixed(0) + " ppm", co2 === null ? 0 : clamp01(co2 / 2000), false)}
  `;
  return div;
}

function gaugeRow(label, valueText, pct, isAlert) {
  return `
    <div class="gauge-row">
      <span class="gauge-label">${label}</span>
      <span class="gauge-track"><span class="gauge-fill${isAlert ? " alert" : ""}" style="width:${Math.round(pct * 100)}%"></span></span>
      <span class="gauge-value">${valueText}</span>
    </div>`;
}

// ===== Telemetry =====
async function loadModulesForTelemetry() {
  const modules = await api("/api/lunar/modules");
  const sel = document.getElementById("telemetryModuleSelect");
  const historySel = document.getElementById("telemetryHistorySelect");
  sel.innerHTML = "";
  const prevHistoryValue = historySel.value;
  historySel.innerHTML = "";
  historySel.appendChild(option("", "Select a module…"));
  modules.forEach(m => {
    sel.appendChild(option(m.id, m.name));
    historySel.appendChild(option(m.id, m.name));
  });
  if (prevHistoryValue) historySel.value = prevHistoryValue;
}

async function loadTelemetryHistory(moduleId) {
  const tbody = document.querySelector("#telemetryTable tbody");
  if (!moduleId) {
    tbody.innerHTML = '<tr><td colspan="8" class="empty">Select a module to view its log.</td></tr>';
    return;
  }
  try {
    const readings = await api(`/api/lunar/modules/${moduleId}/telemetry`);
    if (!readings.length) {
      tbody.innerHTML = '<tr><td colspan="8" class="empty">No readings logged yet.</td></tr>';
      return;
    }
    tbody.innerHTML = "";
    readings.forEach(r => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${shortDate(r.timestamp)}</td>
        <td>${Number(r.atmosphericPressureKPa).toFixed(2)}</td>
        <td>${Number(r.waterPurityPct).toFixed(2)}</td>
        <td>${Number(r.oxygenLevelPct).toFixed(2)}</td>
        <td>${Number(r.co2LevelPpm).toFixed(0)}</td>
        <td>${Number(r.oxygenConsumedM3).toFixed(2)}</td>
        <td>${Number(r.waterConsumedLiters).toFixed(2)}</td>
        <td>${r.scrubberAdjusted ? '<span class="chip adjusted">Adjusted</span>' : "—"}</td>`;
      tbody.appendChild(tr);
    });
  } catch (e) { toast("Could not load telemetry log: " + e.message, true); }
}

// ===== Orders & invoices =====
async function loadTenantsForOrder() {
  const contacts = await api("/api/contacts");
  const sel = document.getElementById("orderTenantSelect");
  sel.innerHTML = "";
  contacts.filter(c => c.type === "CUSTOMER").forEach(c => sel.appendChild(option(c.id, c.name)));
}

async function loadModulesForOrder() {
  const modules = await api("/api/lunar/modules");
  const sel = document.getElementById("orderModuleSelect");
  sel.innerHTML = "";
  modules.forEach(m => sel.appendChild(option(m.id, m.name)));
}

async function loadProductsForOrder() {
  const products = await api("/api/products");
  const sel = document.getElementById("orderProductSelect");
  sel.innerHTML = "";
  products.forEach(p => sel.appendChild(option(p.id, `${p.name} (${money(p.price)}/${p.unit})`)));
}

async function loadSalesOrders() {
  const tbody = document.querySelector("#ordersTable tbody");
  try {
    const orders = await api("/api/lunar/sales-orders");
    if (!orders.length) {
      tbody.innerHTML = '<tr><td colspan="7" class="empty">No orders yet.</td></tr>';
      return;
    }
    tbody.innerHTML = "";
    orders.forEach(o => {
      const invoiced = o.status === "INVOICED";
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${o.id}</td>
        <td>${o.tenant ? o.tenant.name : "—"}</td>
        <td>${o.module ? o.module.name : "—"}</td>
        <td>${o.product ? o.product.name : "—"}</td>
        <td>${o.periodStart} → ${o.periodEnd}</td>
        <td><span class="chip ${invoiced ? "invoiced" : "confirmed"}">${o.status}</span></td>
        <td>${invoiced ? "—" : `<button class="btn small" data-invoice="${o.id}">Convert to invoice</button>`}</td>`;
      tbody.appendChild(tr);
    });
    tbody.querySelectorAll("[data-invoice]").forEach(btn => {
      btn.addEventListener("click", () => convertToInvoice(btn.dataset.invoice));
    });
  } catch (e) { toast("Could not load sales orders: " + e.message, true); }
}

async function convertToInvoice(orderId) {
  try {
    const invoice = await api(`/api/lunar/sales-orders/${orderId}/invoice`, "POST");
    toast(`Invoice #${invoice.id} generated for ${money(invoice.total)}`);
    loadSalesOrders();
    loadInvoices();
  } catch (e) { toast("Could not generate invoice: " + e.message, true); }
}

async function loadInvoices() {
  const tbody = document.querySelector("#invoicesTable tbody");
  try {
    const invoices = await api("/api/invoices");
    if (!invoices.length) {
      tbody.innerHTML = '<tr><td colspan="6" class="empty">No invoices yet — convert a sales order first.</td></tr>';
      return;
    }
    tbody.innerHTML = "";
    invoices.forEach(inv => {
      const paid = inv.status === "PAID";
      const tenant = inv.salesOrder && inv.salesOrder.tenant ? inv.salesOrder.tenant.name : "—";
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${inv.id}</td>
        <td>${tenant}</td>
        <td>${Number(inv.quantity).toFixed(2)}</td>
        <td>${money(inv.total)}</td>
        <td><span class="chip ${paid ? "paid" : "unpaid"}">${inv.status}</span></td>
        <td>${paid ? "—" : `
          <select data-method-for="${inv.id}">
            <option value="BANK_TRANSFER">Bank transfer</option>
            <option value="CASH">Cash</option>
            <option value="CARD">Card</option>
          </select>
          <button class="btn small" data-pay="${inv.id}">Collect</button>`}</td>`;
      tbody.appendChild(tr);
    });
    tbody.querySelectorAll("[data-pay]").forEach(btn => {
      btn.addEventListener("click", () => collectPayment(btn.dataset.pay));
    });
  } catch (e) { toast("Could not load invoices: " + e.message, true); }
}

async function collectPayment(invoiceId) {
  const methodSelect = document.querySelector(`[data-method-for="${invoiceId}"]`);
  const method = methodSelect ? methodSelect.value : "BANK_TRANSFER";
  try {
    await api("/api/payments", "POST", { invoiceId: Number(invoiceId), method });
    toast(`Payment collected for invoice #${invoiceId}`);
    loadInvoices();
  } catch (e) { toast("Payment failed: " + e.message, true); }
}

// ===== Master data =====
async function loadContacts() {
  const tbody = document.querySelector("#contactsTable tbody");
  try {
    const contacts = await api("/api/contacts");
    if (!contacts.length) {
      tbody.innerHTML = '<tr><td colspan="4" class="empty">No contacts yet.</td></tr>';
      return;
    }
    tbody.innerHTML = "";
    contacts.forEach(c => {
      const tr = document.createElement("tr");
      tr.innerHTML = `<td>${c.id}</td><td>${c.name}</td><td>${c.type}</td><td>${c.phone || "—"}</td>`;
      tbody.appendChild(tr);
    });
  } catch (e) { toast("Could not load contacts: " + e.message, true); }
}

async function loadProducts() {
  const tbody = document.querySelector("#productsTable tbody");
  try {
    const products = await api("/api/products");
    if (!products.length) {
      tbody.innerHTML = '<tr><td colspan="5" class="empty">No products yet.</td></tr>';
      return;
    }
    tbody.innerHTML = "";
    products.forEach(p => {
      const tr = document.createElement("tr");
      tr.innerHTML = `<td>${p.id}</td><td>${p.name}</td><td>${p.category}</td><td>${p.unit}</td><td>${money(p.price)}</td>`;
      tbody.appendChild(tr);
    });
  } catch (e) { toast("Could not load products: " + e.message, true); }
}

async function loadAccounts() {
  const tbody = document.querySelector("#accountsTable tbody");
  try {
    const accounts = await api("/api/accounts");
    if (!accounts.length) {
      tbody.innerHTML = '<tr><td colspan="3" class="empty">No accounts posted yet.</td></tr>';
      return;
    }
    tbody.innerHTML = "";
    accounts.forEach(a => {
      const tr = document.createElement("tr");
      tr.innerHTML = `<td>${a.code}</td><td>${a.name}</td><td>${a.type}</td>`;
      tbody.appendChild(tr);
    });
  } catch (e) { toast("Could not load accounts: " + e.message, true); }
}

// ===== Procurement =====
async function loadVendorsAndProducts() {
  const contacts = await api("/api/contacts");
  const vendorSelect = document.getElementById("poVendorSelect");
  vendorSelect.innerHTML = "";
  contacts.filter(c => c.type === "VENDOR").forEach(v => vendorSelect.appendChild(option(v.id, v.name)));

  const products = await api("/api/products");
  const productSelect = document.getElementById("poProductSelect");
  productSelect.innerHTML = "";
  products.forEach(p => productSelect.appendChild(option(p.id, `${p.name} (${money(p.price)})`)));
}

async function loadPurchaseOrders() {
  const tbody = document.querySelector("#poTable tbody");
  try {
    const orders = await api("/api/purchase/orders");
    if (!orders.length) {
      tbody.innerHTML = '<tr><td colspan="7" class="empty">No purchase orders yet.</td></tr>';
      return;
    }
    tbody.innerHTML = "";
    orders.forEach(o => {
      const billed = o.status === "BILLED";
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${o.id}</td>
        <td>${o.vendor ? o.vendor.name : "—"}</td>
        <td>${o.product ? o.product.name : "—"}</td>
        <td>${o.quantity}</td>
        <td>${money(o.unitPrice)}</td>
        <td><span class="chip ${billed ? "invoiced" : "confirmed"}">${o.status}</span></td>
        <td>${billed ? "—" : `<button class="btn small" data-bill="${o.id}">Convert to bill</button>`}</td>`;
      tbody.appendChild(tr);
    });
    tbody.querySelectorAll("[data-bill]").forEach(btn => {
      btn.addEventListener("click", () => convertToBill(btn.dataset.bill));
    });
  } catch (e) { toast("Could not load purchase orders: " + e.message, true); }
}

async function convertToBill(orderId) {
  try {
    const bill = await api(`/api/purchase/orders/${orderId}/bill`, "POST");
    toast(`Vendor bill #${bill.id} created for ${money(bill.amount)}`);
    loadPurchaseOrders();
    loadBills();
    refreshSectionIfActive("master", loadAccounts);
  } catch (e) { toast("Could not create bill: " + e.message, true); }
}

async function loadBills() {
  const tbody = document.querySelector("#billsTable tbody");
  try {
    const bills = await api("/api/purchase/bills");
    if (!bills.length) {
      tbody.innerHTML = '<tr><td colspan="4" class="empty">No bills yet.</td></tr>';
      return;
    }
    tbody.innerHTML = "";
    bills.forEach(b => {
      const paid = b.status === "PAID";
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${b.id}</td>
        <td>${money(b.amount)}</td>
        <td><span class="chip ${paid ? "paid" : "unpaid"}">${b.status}</span></td>
        <td>${paid ? "—" : `<button class="btn small" data-paybill="${b.id}">Pay via bank</button>`}</td>`;
      tbody.appendChild(tr);
    });
    tbody.querySelectorAll("[data-paybill]").forEach(btn => {
      btn.addEventListener("click", () => payBill(btn.dataset.paybill));
    });
  } catch (e) { toast("Could not load bills: " + e.message, true); }
}

async function payBill(billId) {
  try {
    await api(`/api/purchase/bills/${billId}/pay`, "PUT");
    toast(`Bill #${billId} paid`);
    loadBills();
    refreshSectionIfActive("master", loadAccounts);
  } catch (e) { toast("Payment failed: " + e.message, true); }
}

function refreshSectionIfActive(name, fn) {
  const section = document.getElementById("section-" + name);
  if (section && section.classList.contains("is-active")) fn();
}

// ===== Budgets =====
async function loadModulesForAnalytic() {
  const modules = await api("/api/lunar/modules");
  const sel = document.getElementById("analyticModuleSelect");
  sel.innerHTML = "";
  modules.forEach(m => sel.appendChild(option(m.id, m.name)));
}

async function loadAnalyticAccounts() {
  const tbody = document.querySelector("#analyticTable tbody");
  const budgetSelect = document.getElementById("budgetAnalyticSelect");
  try {
    const accounts = await api("/api/budget/analytic-accounts");
    if (!accounts.length) {
      tbody.innerHTML = '<tr><td colspan="3" class="empty">None yet.</td></tr>';
    } else {
      tbody.innerHTML = "";
      accounts.forEach(a => {
        const tr = document.createElement("tr");
        tr.innerHTML = `<td>${a.id}</td><td>${a.name}</td><td>${a.module ? a.module.name : "—"}</td>`;
        tbody.appendChild(tr);
      });
    }
    budgetSelect.innerHTML = "";
    accounts.forEach(a => budgetSelect.appendChild(option(a.id, a.name)));
  } catch (e) { toast("Could not load analytic accounts: " + e.message, true); }
}

async function loadBudgets() {
  const tbody = document.querySelector("#budgetsTable tbody");
  try {
    const budgets = await api("/api/budget/budgets");
    if (!budgets.length) {
      tbody.innerHTML = '<tr><td colspan="3" class="empty">None yet.</td></tr>';
      return;
    }
    tbody.innerHTML = "";
    budgets.forEach(b => {
      const tr = document.createElement("tr");
      tr.innerHTML = `<td>${b.id}</td><td>${money(b.plannedAmount)}</td><td>${b.periodStart} → ${b.periodEnd}</td>`;
      tbody.appendChild(tr);
    });
  } catch (e) { toast("Could not load budgets: " + e.message, true); }
}

// ===== Reports =====
async function loadAnalyticForVariance() {
  const accounts = await api("/api/budget/analytic-accounts");
  const sel = document.getElementById("budgetVarianceSelect");
  sel.innerHTML = "";
  accounts.forEach(a => sel.appendChild(option(a.id, a.name)));
}

function renderReportLines(container, entries, totalLabel, totalValue) {
  container.innerHTML = "";
  entries.forEach(([label, value]) => {
    const row = document.createElement("div");
    row.className = "report-line";
    row.innerHTML = `<span>${label}</span><span>${money(value)}</span>`;
    container.appendChild(row);
  });
  const totalRow = document.createElement("div");
  totalRow.className = "report-line total";
  totalRow.innerHTML = `<span>${totalLabel}</span><span>${money(totalValue)}</span>`;
  container.appendChild(totalRow);
}

async function runProfitAndLoss() {
  const box = document.getElementById("reportPL");
  try {
    const r = await api("/api/reports/profit-and-loss");
    box.innerHTML = "";
    renderReportLines(box, Object.entries(r.income), "Total income", r.totalIncome);
    const expenseBox = document.createElement("div");
    box.appendChild(expenseBox);
    renderReportLines(expenseBox, Object.entries(r.expenses), "Total expense", r.totalExpense);
    const netRow = document.createElement("div");
    netRow.className = "report-line total";
    netRow.innerHTML = `<span>Net profit</span><span>${money(r.netProfit)}</span>`;
    box.appendChild(netRow);
  } catch (e) { toast("Report failed: " + e.message, true); }
}

async function runBalanceSheet() {
  const box = document.getElementById("reportBS");
  try {
    const r = await api("/api/reports/balance-sheet");
    box.innerHTML = "";
    renderReportLines(box, Object.entries(r.assets), "Total assets", r.totalAssets);
    const liabBox = document.createElement("div");
    box.appendChild(liabBox);
    renderReportLines(liabBox, Object.entries(r.liabilities), "Total liabilities", r.totalLiabilities);
    const eqRow = document.createElement("div");
    eqRow.className = "report-line total";
    eqRow.innerHTML = `<span>Equity</span><span>${money(r.equity)}</span>`;
    box.appendChild(eqRow);
  } catch (e) { toast("Report failed: " + e.message, true); }
}

async function runRevenueByModule() {
  const box = document.getElementById("reportRevenue");
  try {
    const r = await api("/api/reports/revenue-by-module");
    const entries = Object.entries(r);
    if (!entries.length) { box.innerHTML = '<p class="empty">No modules yet.</p>'; return; }
    box.innerHTML = "";
    entries.forEach(([mod, total]) => {
      const row = document.createElement("div");
      row.className = "report-line";
      row.innerHTML = `<span>${mod}</span><span>${money(total)}</span>`;
      box.appendChild(row);
    });
  } catch (e) { toast("Report failed: " + e.message, true); }
}

async function runBudgetVariance() {
  const box = document.getElementById("reportBudget");
  const sel = document.getElementById("budgetVarianceSelect");
  if (!sel.value) { toast("Create an analytic account first", true); return; }
  try {
    const r = await api(`/api/reports/budget-variance/${sel.value}`);
    box.innerHTML = "";
    renderReportLines(box, [
      ["Planned", r.plannedAmount],
      ["Actual", r.actualAmount],
    ], "Variance", r.variance);
  } catch (e) { toast("Report failed: " + e.message, true); }
}

// ===== Form bindings =====
function bindForms() {
  document.getElementById("formCreateModule").addEventListener("submit", async (ev) => {
    ev.preventDefault();
    const f = ev.target;
    try {
      await api("/api/lunar/modules", "POST", {
        name: f.name.value,
        zone: f.zone.value,
        domeAreaM2: Number(f.domeAreaM2.value),
        pressureMinKPa: Number(f.pressureMinKPa.value),
        pressureMaxKPa: Number(f.pressureMaxKPa.value),
        waterPurityMinPct: Number(f.waterPurityMinPct.value),
      });
      toast("Habitat module commissioned");
      f.reset();
      loadModules();
    } catch (e) { toast("Could not commission module: " + e.message, true); }
  });

  document.getElementById("formTelemetry").addEventListener("submit", async (ev) => {
    ev.preventDefault();
    const f = ev.target;
    try {
      const reading = await api("/api/lunar/telemetry", "POST", {
        moduleId: Number(f.moduleId.value),
        atmosphericPressureKPa: Number(f.atmosphericPressureKPa.value),
        waterPurityPct: Number(f.waterPurityPct.value),
        oxygenLevelPct: Number(f.oxygenLevelPct.value),
        co2LevelPpm: Number(f.co2LevelPpm.value),
        oxygenConsumedM3: Number(f.oxygenConsumedM3.value || 0),
        waterConsumedLiters: Number(f.waterConsumedLiters.value || 0),
      });
      document.getElementById("telemetryResult").innerHTML = `
        <div class="report-line"><span>Reading logged</span><span>${shortDate(reading.timestamp)}</span></div>
        <div class="report-line"><span>Scrubber action</span><span>${reading.scrubberAdjusted ? "Auto-adjusted (threshold breach)" : "No adjustment needed"}</span></div>`;
      toast(reading.scrubberAdjusted ? "Threshold breached — scrubbers auto-adjusted" : "Reading nominal");
      loadTelemetryHistory(document.getElementById("telemetryHistorySelect").value);
      refreshSectionIfActive("modules", loadModules);
    } catch (e) { toast("Telemetry ingestion failed: " + e.message, true); }
  });

  document.getElementById("telemetryHistorySelect").addEventListener("change", (ev) => {
    loadTelemetryHistory(ev.target.value);
  });

  document.getElementById("formSalesOrder").addEventListener("submit", async (ev) => {
    ev.preventDefault();
    const f = ev.target;
    try {
      await api("/api/lunar/sales-orders", "POST", {
        tenantId: Number(f.tenantId.value),
        moduleId: Number(f.moduleId.value),
        productId: Number(f.productId.value),
        periodStart: f.periodStart.value,
        periodEnd: f.periodEnd.value,
      });
      toast("Service order issued");
      f.reset();
      loadSalesOrders();
    } catch (e) { toast("Could not issue order: " + e.message, true); }
  });

  document.getElementById("formContact").addEventListener("submit", async (ev) => {
    ev.preventDefault();
    const f = ev.target;
    try {
      await api("/api/contacts", "POST", {
        name: f.name.value,
        type: f.type.value,
        phone: f.phone.value,
        email: f.email.value,
      });
      toast("Contact added");
      f.reset();
      loadContacts();
    } catch (e) { toast("Could not add contact: " + e.message, true); }
  });

  document.getElementById("formProduct").addEventListener("submit", async (ev) => {
    ev.preventDefault();
    const f = ev.target;
    try {
      await api("/api/products", "POST", {
        name: f.name.value,
        category: f.category.value,
        unit: f.unit.value,
        price: Number(f.price.value),
      });
      toast("Product added");
      f.reset();
      loadProducts();
    } catch (e) { toast("Could not add product: " + e.message, true); }
  });

  document.getElementById("formPurchaseOrder").addEventListener("submit", async (ev) => {
    ev.preventDefault();
    const f = ev.target;
    try {
      await api("/api/purchase/orders", "POST", {
        vendor: { id: Number(f.vendorId.value) },
        product: { id: Number(f.productId.value) },
        quantity: Number(f.quantity.value),
        unitPrice: Number(f.unitPrice.value),
      });
      toast("Purchase order created");
      f.reset();
      loadPurchaseOrders();
    } catch (e) { toast("Could not create purchase order: " + e.message, true); }
  });

  document.getElementById("formAnalytic").addEventListener("submit", async (ev) => {
    ev.preventDefault();
    const f = ev.target;
    try {
      await api("/api/budget/analytic-accounts", "POST", {
        name: f.name.value,
        module: { id: Number(f.moduleId.value) },
      });
      toast("Analytic account created");
      f.reset();
      loadAnalyticAccounts();
    } catch (e) { toast("Could not create analytic account: " + e.message, true); }
  });

  document.getElementById("formBudget").addEventListener("submit", async (ev) => {
    ev.preventDefault();
    const f = ev.target;
    try {
      await api("/api/budget/budgets", "POST", {
        analyticAccount: { id: Number(f.analyticAccountId.value) },
        plannedAmount: Number(f.plannedAmount.value),
        periodStart: f.periodStart.value,
        periodEnd: f.periodEnd.value,
      });
      toast("Budget line added");
      f.reset();
      loadBudgets();
    } catch (e) { toast("Could not add budget line: " + e.message, true); }
  });

  document.querySelectorAll("[data-report]").forEach(btn => {
    btn.addEventListener("click", () => {
      const kind = btn.dataset.report;
      if (kind === "pl") runProfitAndLoss();
      if (kind === "bs") runBalanceSheet();
      if (kind === "revenue") runRevenueByModule();
    });
  });
  document.getElementById("runBudgetVariance").addEventListener("click", runBudgetVariance);
}

// ===== Boot =====
document.addEventListener("DOMContentLoaded", () => {
  initTabs();
  bindForms();
  tickClock();
  setInterval(tickClock, 1000);
  checkApiStatus();
  setInterval(checkApiStatus, 15000);
  loadModules();
});
