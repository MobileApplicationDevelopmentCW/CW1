package my.foodon.pizzamania.cart;

import java.util.HashMap;
import java.util.Map;

public class CartRow {
    public String pizzaId;
    public String name;
    public String sizeCode;   // "S","M","L"
    public String sizeLabel;  // "Small","Medium","Large"
    public String imageUrl;   // optional
    public double unitPrice;  // price snapshot at add time
    public int quantity;
    public long updatedAt;    // server timestamp (millis)

    // Required empty constructor for Firebase
    public CartRow() {}

    // Constructor without updatedAt (Firebase sets it automatically)
    public CartRow(String pizzaId, String name, String sizeCode, String sizeLabel,
                   String imageUrl, double unitPrice, int quantity) {
        this.pizzaId = pizzaId;
        this.name = name;
        this.sizeCode = sizeCode;
        this.sizeLabel = sizeLabel;
        this.imageUrl = imageUrl;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    // Full constructor (if you want to set updatedAt manually)
    public CartRow(String pizzaId, String name, String sizeCode, String sizeLabel,
                   String imageUrl, double unitPrice, int quantity, long updatedAt) {
        this.pizzaId = pizzaId;
        this.name = name;
        this.sizeCode = sizeCode;
        this.sizeLabel = sizeLabel;
        this.imageUrl = imageUrl;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.updatedAt = updatedAt;
    }

    // --- Getters and setters ---
    public String getPizzaId() { return pizzaId; }
    public void setPizzaId(String pizzaId) { this.pizzaId = pizzaId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSizeCode() { return sizeCode; }
    public void setSizeCode(String sizeCode) { this.sizeCode = sizeCode; }

    public String getSizeLabel() { return sizeLabel; }
    public void setSizeLabel(String sizeLabel) { this.sizeLabel = sizeLabel; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    // ✅ Utility: convert to Firebase-friendly Map
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("pizzaId", pizzaId);
        m.put("name", name);
        m.put("sizeCode", sizeCode);
        m.put("sizeLabel", sizeLabel);
        if (imageUrl != null) m.put("imageUrl", imageUrl);
        m.put("unitPrice", unitPrice);
        m.put("quantity", quantity);
        m.put("updatedAt", updatedAt);
        return m;
    }
}