package model;

import java.math.BigDecimal;

public class Product {
    private int productId;
    private String productName;
    private String description;
    private BigDecimal defaultSellingPrice;
    private int brandId;
    private int categoryId;

    // Extra display fields for joined queries
    private String brandName;
    private String categoryName;

    public Product() {
    }

    public Product(String productName, String description, BigDecimal defaultSellingPrice, int brandId, int categoryId) {
        this.productName = productName;
        this.description = description;
        this.defaultSellingPrice = defaultSellingPrice;
        this.brandId = brandId;
        this.categoryId = categoryId;
    }

    public Product(int productId, String productName, String description, BigDecimal defaultSellingPrice, int brandId, int categoryId) {
        this.productId = productId;
        this.productName = productName;
        this.description = description;
        this.defaultSellingPrice = defaultSellingPrice;
        this.brandId = brandId;
        this.categoryId = categoryId;
    }

    public Product(int productId, String productName, String description, BigDecimal defaultSellingPrice,
                   int brandId, String brandName, int categoryId, String categoryName) {
        this.productId = productId;
        this.productName = productName;
        this.description = description;
        this.defaultSellingPrice = defaultSellingPrice;
        this.brandId = brandId;
        this.brandName = brandName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getDefaultSellingPrice() {
        return defaultSellingPrice;
    }

    public void setDefaultSellingPrice(BigDecimal defaultSellingPrice) {
        this.defaultSellingPrice = defaultSellingPrice;
    }

    public int getBrandId() {
        return brandId;
    }

    public void setBrandId(int brandId) {
        this.brandId = brandId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public String toString() {
        return productName;
    }
}