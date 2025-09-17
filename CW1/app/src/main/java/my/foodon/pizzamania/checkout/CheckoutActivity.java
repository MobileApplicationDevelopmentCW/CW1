package my.foodon.pizzamania.checkout;

import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import my.foodon.pizzamania.R;
import my.foodon.pizzamania.cart.CartRow;
import my.foodon.pizzamania.cart.FirebaseCart;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CheckoutActivity extends AppCompatActivity {

    // Your publishable key (must start with pk_)
    private static final String STRIPE_PUBLISHABLE_KEY =
            "pk_test_51RMTa8RcL6DL1aURydPa2GKgP4lcvaKSw6349cgErK61vxDghFQUYgqaVOO2Fln6HVA2xFqyeoJHvXVdnskMyzQI00QHvYr7yQ";

    private static final String FUNCTIONS_REGION = "asia-south1"; // used ONLY for Stripe

    private EditText nameEt, phoneEt, addressEt;
    private TextView amountTxt;
    private RecyclerView itemsRv;
    private RadioGroup payGroup;
    private Button btnPlace;
    private final DecimalFormat money = new DecimalFormat("#,##0.00");

    private final List<CartRow> rows = new ArrayList<>();
    private double total = 0.0;

    // Stripe
    private PaymentSheet paymentSheet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // ✅ Change Status Bar Color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.black_background)); // #060606
        }

        // Stripe init (required before using PaymentSheet)
        PaymentConfiguration.init(getApplicationContext(), STRIPE_PUBLISHABLE_KEY);

        nameEt   = findViewById(R.id.etName);
        phoneEt  = findViewById(R.id.etPhone);
        addressEt= findViewById(R.id.etAddress);
        amountTxt= findViewById(R.id.txtAmount);
        itemsRv  = findViewById(R.id.rvItems);
        payGroup = findViewById(R.id.payGroup);
        btnPlace = findViewById(R.id.btnPlaceOrder);

        itemsRv.setLayoutManager(new LinearLayoutManager(this));
        itemsRv.setAdapter(new SimpleCartAdapter(rows));

        fetchProfile();
        fetchCart();

        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        btnPlace.setOnClickListener(v -> {
            int checked = payGroup.getCheckedRadioButtonId();
            if (checked == R.id.rbCOD) {
                placeCodOrder();   // ✅ local DB write, no region
            } else if (checked == R.id.rbCard) {
                startStripeFlow(); // ✅ callable in asia-south1
            } else {
                Toast.makeText(this, "Select a payment method", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String uid() {
        return Objects.requireNonNull(FirebaseAuth.getInstance().getUid());
    }

    private void fetchProfile() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot s) {
                String n = s.child("name").getValue(String.class);
                String p = s.child("phone").getValue(String.class);
                String a = s.child("address").getValue(String.class);
                if (n != null) nameEt.setText(n);
                if (p != null) phoneEt.setText(p);
                if (a != null) addressEt.setText(a);
            }
            @Override public void onCancelled(DatabaseError e) { }
        });
    }

    private void fetchCart() {
        FirebaseCart.observeCart(uid(), new FirebaseCart.CartListener() {
            @Override public void onCartChanged(List<CartRow> newRows) {
                rows.clear();
                rows.addAll(newRows);
                itemsRv.getAdapter().notifyDataSetChanged();
                total = 0;
                for (CartRow r : rows) total += r.unitPrice * r.quantity;
                amountTxt.setText("Rs " + money.format(total));
            }
            @Override public void onError(String message) {
                Toast.makeText(CheckoutActivity.this, "Cart load failed: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------------- COD: local write (no Functions, no region) ----------------
    private void placeCodOrder() {
        if (!validateProfile()) return;
        setLoading(true, "Placing...");

        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        String orderId = root.child("orders").child(uid()).push().getKey();

        Map<String, Object> order = new HashMap<>();
        order.put("orderId", orderId);
        order.put("uid", uid());
        order.put("status", "placed");           // initial status
        order.put("paymentMethod", "COD");
        order.put("paid", false);
        order.put("createdAt", System.currentTimeMillis());
        order.put("amount", total);
        order.put("customerName", nameEt.getText().toString().trim());
        order.put("customerPhone", phoneEt.getText().toString().trim());
        order.put("customerAddress", addressEt.getText().toString().trim());
        order.put("items", buildItems());

        Map<String, Object> updates = new HashMap<>();
        updates.put("/orders/" + uid() + "/" + orderId, order);
        updates.put("/carts/" + uid(), null); // clear cart

        root.updateChildren(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Order placed (COD).", Toast.LENGTH_LONG).show();
                    finish(); // back to home
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Order failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    setLoading(false, "Place Order");
                });
    }

    // ---------------- Stripe: callable createPaymentIntent, then save order ----------------
    private void startStripeFlow() {
        if (!validateProfile()) return;
        setLoading(true, "Processing...");

        Map<String, Object> data = new HashMap<>();
        data.put("amount", Math.round(total * 100)); // minor units
        data.put("currency", "usd"); // ensure your Stripe account supports this

        FirebaseFunctions.getInstance(FUNCTIONS_REGION)
                .getHttpsCallable("createPaymentIntent")
                .call(data)
                .addOnSuccessListener((HttpsCallableResult result) -> {
                    Object raw = result.getData();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> resp = (Map<String, Object>) raw;
                    String clientSecret = resp != null ? (String) resp.get("clientSecret") : null;

                    if (clientSecret == null || clientSecret.isEmpty()) {
                        Toast.makeText(this, "Stripe: clientSecret missing", Toast.LENGTH_LONG).show();
                        setLoading(false, "Place Order");
                        return;
                    }
                    PaymentSheet.Configuration config = new PaymentSheet.Configuration("PizzaMania");
                    paymentSheet.presentWithPaymentIntent(clientSecret, config);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Stripe init failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    setLoading(false, "Place Order");
                });
    }

    private void onPaymentSheetResult(final PaymentSheetResult result) {
        if (result instanceof PaymentSheetResult.Completed) {
            // Payment succeeded → save order as paid CARD
            savePaidCardOrder();
        } else if (result instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show();
            setLoading(false, "Place Order");
        } else if (result instanceof PaymentSheetResult.Failed) {
            PaymentSheetResult.Failed f = (PaymentSheetResult.Failed) result;
            Toast.makeText(this, "Payment failed: " + f.getError().getMessage(), Toast.LENGTH_LONG).show();
            setLoading(false, "Place Order");
        }
    }

    private void savePaidCardOrder() {
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        String orderId = root.child("orders").child(uid()).push().getKey();

        Map<String, Object> order = new HashMap<>();
        order.put("orderId", orderId);
        order.put("uid", uid());
        order.put("status", "paid");
        order.put("paymentMethod", "CARD");
        order.put("paid", true);
        order.put("createdAt", System.currentTimeMillis());
        order.put("amount", total);
        order.put("customerName", nameEt.getText().toString().trim());
        order.put("customerPhone", phoneEt.getText().toString().trim());
        order.put("customerAddress", addressEt.getText().toString().trim());
        order.put("items", buildItems());

        Map<String, Object> updates = new HashMap<>();
        updates.put("/orders/" + uid() + "/" + orderId, order);
        updates.put("/carts/" + uid(), null);

        root.updateChildren(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Payment successful. Order placed.", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Order save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    setLoading(false, "Place Order");
                });
    }

    // ---------------- Helpers ----------------
    private boolean validateProfile() {
        if (nameEt.getText().toString().trim().isEmpty()
                || phoneEt.getText().toString().trim().isEmpty()
                || addressEt.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please fill name, phone and address", Toast.LENGTH_SHORT).show();
            return false;
        }
        // Persist profile for next checkout
        DatabaseReference uref = FirebaseDatabase.getInstance().getReference("users").child(uid());
        Map<String, Object> profile = new HashMap<>();
        profile.put("name", nameEt.getText().toString().trim());
        profile.put("phone", phoneEt.getText().toString().trim());
        profile.put("address", addressEt.getText().toString().trim());
        uref.updateChildren(profile);
        return true;
    }

    private List<Map<String, Object>> buildItems() {
        List<Map<String, Object>> items = new ArrayList<>();
        for (CartRow r : rows) {
            Map<String, Object> it = new HashMap<>();
            it.put("pizzaId", r.pizzaId);
            it.put("name", r.name);
            it.put("sizeCode", r.sizeCode);
            it.put("sizeLabel", r.sizeLabel);
            it.put("unitPrice", r.unitPrice);
            it.put("quantity", r.quantity);
            if (r.imageUrl != null) it.put("imageUrl", r.imageUrl);
            items.add(it);
        }
        return items;
    }

    private void setLoading(boolean loading, String label) {
        btnPlace.setEnabled(!loading);
        btnPlace.setText(label);
    }

    // ---- Simple summary adapter (needs checkout_item.xml) ----
    static class SimpleCartAdapter extends RecyclerView.Adapter<SimpleCartAdapter.VH> {
        private final List<CartRow> data;
        SimpleCartAdapter(List<CartRow> d) { data = d; }

        @Override
        public VH onCreateViewHolder(android.view.ViewGroup p, int v) {
            android.view.View row = android.view.LayoutInflater.from(p.getContext())
                    .inflate(R.layout.checkout_item, p, false);
            return new VH(row);
        }
        @Override public void onBindViewHolder(VH h, int pos) { h.bind(data.get(pos)); }
        @Override public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView nameQty, price;
            final DecimalFormat money = new DecimalFormat("#,##0.00");
            VH(android.view.View v) {
                super(v);
                nameQty = v.findViewById(R.id.ciNameQty);
                price   = v.findViewById(R.id.ciPrice);
            }
            void bind(CartRow r) {
                nameQty.setText(r.name + " (" + r.sizeLabel + ") x " + r.quantity);
                price.setText("Rs " + money.format(r.unitPrice * r.quantity));
            }
        }
    }
}