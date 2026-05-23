package model;

import java.math.BigDecimal;

public class PurchaseInvoiceItem {

    private int purchaseItemId;
    private int purchaseInvoiceId;
    private int productId;
    private BigDecimal purchasePrice;
    private int quantity;

    // Display field for joined queries
    private String productName;

    public PurchaseInvoiceItem() {
    }

    public PurchaseInvoiceItem(int productId, BigDecimal purchasePrice, int quantity) {
        this.productId = productId;
        this.purchasePrice = purchasePrice;
        this.quantity = quantity;
    }

    public PurchaseInvoiceItem(int purchaseItemId, int purchaseInvoiceId, int productId,
                               BigDecimal purchasePrice, int quantity) {
        this.purchaseItemId = purchaseItemId;
        this.purchaseInvoiceId = purchaseInvoiceId;
        this.productId = productId;
        this.purchasePrice = purchasePrice;
        this.quantity = quantity;
    }

    public PurchaseInvoiceItem(int purchaseItemId, int purchaseInvoiceId, int productId,
                               String productName, BigDecimal purchasePrice, int quantity) {
        this.purchaseItemId = purchaseItemId;
        this.purchaseInvoiceId = purchaseInvoiceId;
        this.productId = productId;
        this.productName = productName;
        this.purchasePrice = purchasePrice;
        this.quantity = quantity;
    }

    public int getPurchaseItemId() {
        return purchaseItemId;
    }

    public void setPurchaseItemId(int purchaseItemId) {
        this.purchaseItemId = purchaseItemId;
    }

    public int getPurchaseInvoiceId() {
        return purchaseInvoiceId;
    }

    public void setPurchaseInvoiceId(int purchaseInvoiceId) {
        this.purchaseInvoiceId = purchaseInvoiceId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}