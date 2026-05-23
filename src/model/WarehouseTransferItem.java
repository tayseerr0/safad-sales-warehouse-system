package model;

public class WarehouseTransferItem {

    private int transferItemId;
    private int transferId;
    private int productId;
    private int quantity;

    public WarehouseTransferItem() {
    }

    public WarehouseTransferItem(int productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public WarehouseTransferItem(int transferItemId, int transferId, int productId, int quantity) {
        this.transferItemId = transferItemId;
        this.transferId = transferId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public int getTransferItemId() {
        return transferItemId;
    }

    public void setTransferItemId(int transferItemId) {
        this.transferItemId = transferItemId;
    }

    public int getTransferId() {
        return transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}