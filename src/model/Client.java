package model;

import java.time.LocalDate;

public class Client {

    private int clientId;
    private String clientName;
    private String phone;
    private String email;
    private LocalDate registrationDate;
    private String city;
    private String address;
    private String clientType;

    public Client() {
    }

    public Client(String clientName, String phone, String email,
                  LocalDate registrationDate, String city, String address, String clientType) {
        this.clientName = clientName;
        this.phone = phone;
        this.email = email;
        this.registrationDate = registrationDate;
        this.city = city;
        this.address = address;
        this.clientType = clientType;
    }

    public Client(int clientId, String clientName, String phone, String email,
                  LocalDate registrationDate, String city, String address, String clientType) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.phone = phone;
        this.email = email;
        this.registrationDate = registrationDate;
        this.city = city;
        this.address = address;
        this.clientType = clientType;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
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

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
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

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    @Override
    public String toString() {
        return clientName;
    }
}
