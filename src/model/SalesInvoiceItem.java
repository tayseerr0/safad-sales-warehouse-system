package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SalesInvoiceItem {

    private int salesItemId;
    private int salesInvoiceId;
    private int productId;
    private BigDecimal sellingPrice;
    private int quantity;
    private LocalDate warrantyEndDate;

    public SalesInvoiceItem() {
    }

    public SalesInvoiceItem(int productId, BigDecimal sellingPrice, int quantity, LocalDate warrantyEndDate) {
        this.productId = productId;
        this.sellingPrice = sellingPrice;
        this.quantity = quantity;
        this.warrantyEndDate = warrantyEndDate;
    }

    public SalesInvoiceItem(int salesItemId, int salesInvoiceId, int productId,
                            BigDecimal sellingPrice, int quantity, LocalDate warrantyEndDate) {
        this.salesItemId = salesItemId;
        this.salesInvoiceId = salesInvoiceId;
        this.productId = productId;
        this.sellingPrice = sellingPrice;
        this.quantity = quantity;
        this.warrantyEndDate = warrantyEndDate;
    }

    public int getSalesItemId() {
        return salesItemId;
    }

    public void setSalesItemId(int salesItemId) {
        this.salesItemId = salesItemId;
    }

    public int getSalesInvoiceId() {
        return salesInvoiceId;
    }

    public void setSalesInvoiceId(int salesInvoiceId) {
        this.salesInvoiceId = salesInvoiceId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDate getWarrantyEndDate() {
        return warrantyEndDate;
    }

    public void setWarrantyEndDate(LocalDate warrantyEndDate) {
        this.warrantyEndDate = warrantyEndDate;
    }

    public BigDecimal getLineTotal() {
        return sellingPrice.multiply(BigDecimal.valueOf(quantity));
    }
}