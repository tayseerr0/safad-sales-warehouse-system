package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SalesPayment {

    private int salesPaymentId;
    private int salesInvoiceId;
    private LocalDate paymentDate;
    private BigDecimal amount;
    private String paymentType;

    public SalesPayment() {
    }

    public SalesPayment(int salesInvoiceId, LocalDate paymentDate, BigDecimal amount, String paymentType) {
        this.salesInvoiceId = salesInvoiceId;
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.paymentType = paymentType;
    }

    public SalesPayment(int salesPaymentId, int salesInvoiceId, LocalDate paymentDate, BigDecimal amount, String paymentType) {
        this.salesPaymentId = salesPaymentId;
        this.salesInvoiceId = salesInvoiceId;
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.paymentType = paymentType;
    }

    public int getSalesPaymentId() {
        return salesPaymentId;
    }

    public void setSalesPaymentId(int salesPaymentId) {
        this.salesPaymentId = salesPaymentId;
    }

    public int getSalesInvoiceId() {
        return salesInvoiceId;
    }

    public void setSalesInvoiceId(int salesInvoiceId) {
        this.salesInvoiceId = salesInvoiceId;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
}
