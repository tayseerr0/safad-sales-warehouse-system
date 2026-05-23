package model;

import java.time.LocalDate;

public class Supplier {
    private int supplierId;
    private String supplierName;
    private String phone;
    private String email;
    private LocalDate startingDate;
    private String city;
    private String address;

    public Supplier() {
    }

    public Supplier(String supplierName, String phone, String email, LocalDate startingDate, String city, String address) {
        this.supplierName = supplierName;
        this.phone = phone;
        this.email = email;
        this.startingDate = startingDate;
        this.city = city;
        this.address = address;
    }

    public Supplier(int supplierId, String supplierName, String phone, String email, LocalDate startingDate, String city, String address) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.phone = phone;
        this.email = email;
        this.startingDate = startingDate;
        this.city = city;
        this.address = address;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getStartingDate() {
        return startingDate;
    }

    public void setStartingDate(LocalDate startingDate) {
        this.startingDate = startingDate;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return supplierName;
    }
}