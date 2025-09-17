package my.foodon.pizzamania.models;

import java.util.List;
import java.util.Map;

public class Order {
    public String orderId;
    public String uid;
    public String status;           // "placed", "paid", "preparing", "on_the_way", "delivered", "cancelled"
    public String paymentMethod;    // "COD" or "CARD"
    public boolean paid;
    public long createdAt;          // System.currentTimeMillis()
    public double amount;

    public String customerName;
    public String customerPhone;
    public String customerAddress;

    // Each item can be stored as a map: { pizzaId, name, sizeCode, sizeLabel, unitPrice, quantity, imageUrl }
    public List<Map<String, Object>> items;

    // Assigned driver fields (nullable when not assigned)
    public String driverId;     // did
    public String driverName;   // dname
    public String driverPlate;  // dplate
    public String driverPhone;  // Dtel

    // Firebase requires an empty constructor
    public Order() {}

    public Order(String orderId, String uid, String status, String paymentMethod, boolean paid,
                 long createdAt, double amount, String customerName, String customerPhone,
                 String customerAddress, List<Map<String, Object>> items) {
        this.orderId = orderId;
        this.uid = uid;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.paid = paid;
        this.createdAt = createdAt;
        this.amount = amount;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerAddress = customerAddress;
        this.items = items;
    }

    // Getters and Setters for Firebase serialization
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    public List<Map<String, Object>> getItems() { return items; }
    public void setItems(List<Map<String, Object>> items) { this.items = items; }

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getDriverPlate() { return driverPlate; }
    public void setDriverPlate(String driverPlate) { this.driverPlate = driverPlate; }

    public String getDriverPhone() { return driverPhone; }
    public void setDriverPhone(String driverPhone) { this.driverPhone = driverPhone; }
}