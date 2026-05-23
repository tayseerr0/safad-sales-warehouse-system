package model;

import java.math.BigDecimal;

public class SupplierProduct {
    private int supplierId;
    private int productId;
    private BigDecimal supplyPrice;

    // Display fields for joined queries
    private String supplierName;
    private String productName;

    public SupplierProduct() {
    }

    public SupplierProduct(int supplierId, int productId, BigDecimal supplyPrice) {
        this.supplierId = supplierId;
        this.productId = productId;
        this.supplyPrice = supplyPrice;
    }

    public SupplierProduct(int supplierId, String supplierName, int productId, String productName, BigDecimal supplyPrice) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.productId = productId;
        this.productName = productName;
        this.supplyPrice = supplyPrice;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public BigDecimal getSupplyPrice() {
        return supplyPrice;
    }

    public void setSupplyPrice(BigDecimal supplyPrice) {
        this.supplyPrice = supplyPrice;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}