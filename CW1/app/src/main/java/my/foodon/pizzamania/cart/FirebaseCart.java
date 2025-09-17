package my.foodon.pizzamania.cart;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Single-source Firebase cart operations with atomic increment/decrement
public class FirebaseCart {

    private static DatabaseReference cartRef(String uid) {
        return FirebaseDatabase.getInstance().getReference("users").child(uid).child("cart");
    }

    public static String itemKey(String pizzaId, String sizeCode) {
        return pizzaId + "_" + sizeCode; // e.g., "pz123_M"
    }

    public interface CartListener {
        void onCartChanged(List<CartRow> rows);
        void onError(String message);
    }

    // Observe entire cart for current user
    public static ValueEventListener observeCart(String uid, CartListener listener) {
        ValueEventListener vel = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CartRow> out = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    CartRow row = child.getValue(CartRow.class);
                    if (row != null) out.add(row);
                }
                listener.onCartChanged(out);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        };
        cartRef(uid).addValueEventListener(vel);
        return vel;
    }

    public static void removeObserver(String uid, ValueEventListener vel) {
        cartRef(uid).removeEventListener(vel);
    }

    // ✅ Upsert: increment quantity or create new with quantity=1
    public static void upsert(String uid, String pizzaId, String name,
                              String sizeCode, String sizeLabel,
                              @Nullable String imageUrl, double unitPrice) {
        DatabaseReference node = cartRef(uid).child(itemKey(pizzaId, sizeCode));
        node.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Map<String,Object> m = safeMap(currentData.getValue());
                if (m == null) {
                    m = new HashMap<>();
                    m.put("pizzaId", pizzaId);
                    m.put("name", name);
                    m.put("sizeCode", sizeCode);
                    m.put("sizeLabel", sizeLabel);
                    if (imageUrl != null) m.put("imageUrl", imageUrl);
                    m.put("unitPrice", unitPrice);
                    m.put("quantity", 1);
                    m.put("updatedAt", ServerValue.TIMESTAMP);
                    currentData.setValue(m);
                } else {
                    int q = toInt(m.get("quantity"), 0);
                    m.put("quantity", q + 1);
                    m.put("updatedAt", ServerValue.TIMESTAMP);
                    currentData.setValue(m);
                }
                return Transaction.success(currentData);
            }
            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                // Optional log
            }
        });
    }

    // ✅ Wrapper method for compatibility with your UI code
    public static void addOrIncrement(String uid, String pizzaId, String name,
                                      String sizeCode, String sizeLabel,
                                      @Nullable String imageUrl, double unitPrice) {
        upsert(uid, pizzaId, name, sizeCode, sizeLabel, imageUrl, unitPrice);
    }

    // Increment by 1
    public static void increment(String uid, String pizzaId, String sizeCode) {
        DatabaseReference node = cartRef(uid).child(itemKey(pizzaId, sizeCode));
        node.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Map<String,Object> m = safeMap(currentData.getValue());
                if (m == null) return Transaction.success(currentData);
                int q = toInt(m.get("quantity"), 0);
                m.put("quantity", q + 1);
                m.put("updatedAt", ServerValue.TIMESTAMP);
                currentData.setValue(m);
                return Transaction.success(currentData);
            }
            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
            }
        });
    }

    // Decrement by 1; remove when reaches 0
    public static void decrement(String uid, String pizzaId, String sizeCode) {
        DatabaseReference node = cartRef(uid).child(itemKey(pizzaId, sizeCode));
        node.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Map<String,Object> m = safeMap(currentData.getValue());
                if (m == null) return Transaction.success(currentData);
                int q = toInt(m.get("quantity"), 0);
                if (q <= 1) {
                    currentData.setValue(null); // delete node
                } else {
                    m.put("quantity", q - 1);
                    m.put("updatedAt", ServerValue.TIMESTAMP);
                    currentData.setValue(m);
                }
                return Transaction.success(currentData);
            }
            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
            }
        });
    }

    public static void clear(String uid) {
        cartRef(uid).removeValue();
    }

    // Add multiple items at once
    public static void addAll(String uid, List<CartRow> items) {
        DatabaseReference cartNode = cartRef(uid);
        Map<String, Object> updates = new HashMap<>();

        for (CartRow item : items) {
            String key = itemKey(item.getPizzaId(), item.getSizeCode());
            Map<String, Object> m = new HashMap<>();
            m.put("pizzaId", item.getPizzaId());
            m.put("name", item.getName());
            m.put("sizeCode", item.getSizeCode());
            m.put("sizeLabel", item.getSizeLabel());
            if (item.getImageUrl() != null) m.put("imageUrl", item.getImageUrl());
            m.put("unitPrice", item.getUnitPrice());
            m.put("quantity", item.getQuantity() > 0 ? item.getQuantity() : 1);
            m.put("updatedAt", ServerValue.TIMESTAMP);

            updates.put(key, m);
        }

        cartNode.updateChildren(updates);
    }

    // Helper to get current UID or throw
    public static String requireUid() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            throw new IllegalStateException("User not authenticated");
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // --- Internal helpers ---

    @SuppressWarnings("unchecked")
    private static Map<String,Object> safeMap(Object val) {
        if (val == null) return null;
        if (val instanceof Map) {
            Map<?,?> raw = (Map<?,?>) val;
            Map<String,Object> out = new HashMap<>();
            for (Map.Entry<?,?> e : raw.entrySet()) {
                Object k = e.getKey();
                if (k != null) out.put(String.valueOf(k), e.getValue());
            }
            return out;
        }
        return null;
    }

    private static int toInt(Object n, int def) {
        if (n instanceof Number) return ((Number) n).intValue();
        if (n instanceof String) {
            try { return Integer.parseInt((String) n); } catch (Exception ignored) {}
        }
        return def;
    }
}