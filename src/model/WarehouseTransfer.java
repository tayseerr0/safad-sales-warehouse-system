package model;

import java.time.LocalDate;
import java.util.List;

public class WarehouseTransfer {

    private int transferId;
    private LocalDate transferDate;
    private int fromWarehouseId;
    private int toWarehouseId;
    private List<WarehouseTransferItem> items;

    public WarehouseTransfer() {
    }

    public WarehouseTransfer(LocalDate transferDate, int fromWarehouseId, int toWarehouseId, List<WarehouseTransferItem> items) {
        this.transferDate = transferDate;
        this.fromWarehouseId = fromWarehouseId;
        this.toWarehouseId = toWarehouseId;
        this.items = items;
    }

    public WarehouseTransfer(int transferId, LocalDate transferDate, int fromWarehouseId, int toWarehouseId) {
        this.transferId = transferId;
        this.transferDate = transferDate;
        this.fromWarehouseId = fromWarehouseId;
        this.toWarehouseId = toWarehouseId;
    }

    public int getTransferId() {
        return transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }

    public LocalDate getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(LocalDate transferDate) {
        this.transferDate = transferDate;
    }

    public int getFromWarehouseId() {
        return fromWarehouseId;
    }

    public void setFromWarehouseId(int fromWarehouseId) {
        this.fromWarehouseId = fromWarehouseId;
    }

    public int getToWarehouseId() {
        return toWarehouseId;
    }

    public void setToWarehouseId(int toWarehouseId) {
        this.toWarehouseId = toWarehouseId;
    }

    public List<WarehouseTransferItem> getItems() {
        return items;
    }

    public void setItems(List<WarehouseTransferItem> items) {
        this.items = items;
    }
}