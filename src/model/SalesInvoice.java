package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class SalesInvoice {

    private int salesInvoiceId;
    private LocalDate invoiceDate;
    private BigDecimal payment;
    private String paymentType;
    private BigDecimal amount;
    private int clientId;
    private int warehouseId;
    private List<SalesInvoiceItem> items;

    public SalesInvoice() {
    }

    public SalesInvoice(LocalDate invoiceDate, BigDecimal payment, String paymentType,
                        BigDecimal amount, int clientId, int warehouseId,
                        List<SalesInvoiceItem> items) {
        this.invoiceDate = invoiceDate;
        this.payment = payment;
        this.paymentType = paymentType;
        this.amount = amount;
        this.clientId = clientId;
        this.warehouseId = warehouseId;
        this.items = items;
    }

    public SalesInvoice(int salesInvoiceId, LocalDate invoiceDate, BigDecimal payment,
                        String paymentType, BigDecimal amount, int clientId, int warehouseId) {
        this.salesInvoiceId = salesInvoiceId;
        this.invoiceDate = invoiceDate;
        this.payment = payment;
        this.paymentType = paymentType;
        this.amount = amount;
        this.clientId = clientId;
        this.warehouseId = warehouseId;
    }

    public int getSalesInvoiceId() {
        return salesInvoiceId;
    }

    public void setSalesInvoiceId(int salesInvoiceId) {
        this.salesInvoiceId = salesInvoiceId;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
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

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(int warehouseId) {
        this.warehouseId = warehouseId;
    }

    public List<SalesInvoiceItem> getItems() {
        return items;
    }

    public void setItems(List<SalesInvoiceItem> items) {
        this.items = items;
    }
}