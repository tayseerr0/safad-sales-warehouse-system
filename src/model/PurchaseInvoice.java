package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PurchaseInvoice {

    private int purchaseInvoiceId;
    private LocalDate invoiceDate;
    private LocalDate estimatedArrival;
    private BigDecimal payment;
    private String paymentType;
    private BigDecimal amount;
    private int supplierId;
    private int warehouseId;

    // Display fields for joined queries
    private String supplierName;
    private String warehouseName;

    private List<PurchaseInvoiceItem> items;

    public PurchaseInvoice() {
    }

    public PurchaseInvoice(LocalDate invoiceDate, LocalDate estimatedArrival, BigDecimal payment,
                           String paymentType, BigDecimal amount, int supplierId, int warehouseId,
                           List<PurchaseInvoiceItem> items) {
        this.invoiceDate = invoiceDate;
        this.estimatedArrival = estimatedArrival;
        this.payment = payment;
        this.paymentType = paymentType;
        this.amount = amount;
        this.supplierId = supplierId;
        this.warehouseId = warehouseId;
        this.items = items;
    }

    public PurchaseInvoice(int purchaseInvoiceId, LocalDate invoiceDate, LocalDate estimatedArrival,
                           BigDecimal payment, String paymentType, BigDecimal amount,
                           int supplierId, int warehouseId) {
        this.purchaseInvoiceId = purchaseInvoiceId;
        this.invoiceDate = invoiceDate;
        this.estimatedArrival = estimatedArrival;
        this.payment = payment;
        this.paymentType = paymentType;
        this.amount = amount;
        this.supplierId = supplierId;
        this.warehouseId = warehouseId;
    }

    public PurchaseInvoice(int purchaseInvoiceId, LocalDate invoiceDate, LocalDate estimatedArrival,
                           BigDecimal payment, String paymentType, BigDecimal amount,
                           int supplierId, String supplierName, int warehouseId, String warehouseName) {
        this.purchaseInvoiceId = purchaseInvoiceId;
        this.invoiceDate = invoiceDate;
        this.estimatedArrival = estimatedArrival;
        this.payment = payment;
        this.paymentType = paymentType;
        this.amount = amount;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.warehouseId = warehouseId;
        this.warehouseName = warehouseName;
    }

    public int getPurchaseInvoiceId() {
        return purchaseInvoiceId;
    }

    public void setPurchaseInvoiceId(int purchaseInvoiceId) {
        this.purchaseInvoiceId = purchaseInvoiceId;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public LocalDate getEstimatedArrival() {
        return estimatedArrival;
    }

    public void setEstimatedArrival(LocalDate estimatedArrival) {
        this.estimatedArrival = estimatedArrival;
    }

    public BigDecimal getPayment() {
        return payment;
    }

    public void setPayment(BigDecimal payment) {
        this.payment = payment;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public int getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(int warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public List<PurchaseInvoiceItem> getItems() {
        return items;
    }

    public void setItems(List<PurchaseInvoiceItem> items) {
        this.items = items;
    }
}