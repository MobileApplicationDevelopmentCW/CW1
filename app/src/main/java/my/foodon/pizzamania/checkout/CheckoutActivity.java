package my.foodon.pizzamania.checkout;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import java.util.HashMap;
import java.util.Map;

import my.foodon.pizzamania.R;

public class CheckoutActivity extends AppCompatActivity {

    private double cartTotal = 0.0;
    private RadioGroup paymentGroup;
    private TextView totalTxt;
    private Button placeOrderBtn;

    // === Stripe PaymentSheet fields ===
    private PaymentSheet paymentSheet;
    private String publishableKey;
    private String paymentIntentClientSecret;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        cartTotal = getIntent().getDoubleExtra("cart_total", 0.0);

        paymentGroup = findViewById(R.id.paymentGroup);
        totalTxt = findViewById(R.id.checkoutTotalTxt);
        placeOrderBtn = findViewById(R.id.placeOrderBtn);

        totalTxt.setText("Rs " + String.format("%,.2f", cartTotal));

        // === Stripe init ===
        publishableKey = getString(R.string.stripe_publishable_key); // put pk in strings.xml
        PaymentConfiguration.init(getApplicationContext(), publishableKey);
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        placeOrderBtn.setOnClickListener(v -> {
            int selected = paymentGroup.getCheckedRadioButtonId();
            if (selected == R.id.rbCOD) {
                handleCashOnDelivery();
            } else if (selected == R.id.rbCard) {
                startCardPayment();
            } else {
                Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleCashOnDelivery() {
        // TODO: persist order in Firebase with paymentMethod="COD", status="PENDING"
        Toast.makeText(this, "Order placed with Cash on Delivery!", Toast.LENGTH_LONG).show();
        finish();
    }

    // === Stripe: start card payment (PaymentSheet) ===
    private void startCardPayment() {
        // Ask backend (Firebase Function) for a PaymentIntent client secret
        FirebaseFunctions.getInstance()
                .getHttpsCallable("createPaymentIntent")
                .call(buildPaymentData(cartTotal))
                .addOnSuccessListener((HttpsCallableResult r) -> {
                    Object data = r.getData();
                    if (data instanceof Map) {
                        Object secret = ((Map<?, ?>) data).get("clientSecret");
                        if (secret != null) {
                            paymentIntentClientSecret = secret.toString();
                            presentPaymentSheet();
                            return;
                        }
                    }
                    Toast.makeText(this, "Invalid response from payment server.", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to start payment: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private Map<String, Object> buildPaymentData(double total) {
        HashMap<String, Object> m = new HashMap<>();
        long amountInSmallestUnit = Math.round(total * 100); // e.g., cents
        m.put("amount", amountInSmallestUnit);
        m.put("currency", "usd");                   // change to a currency supported by your Stripe account
        m.put("description", "PizzaMania order");
        return m;
    }

    private void presentPaymentSheet() {
        PaymentSheet.Configuration config = new PaymentSheet.Configuration("PizzaMania");
        paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, config);
    }

    private void onPaymentSheetResult(final PaymentSheetResult result) {
        if (result instanceof PaymentSheetResult.Completed) {
            // TODO: save order in Firebase with paymentMethod="CARD", status="PAID"
            Toast.makeText(this, "Payment successful!", Toast.LENGTH_LONG).show();
            finish();
        } else if (result instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(this, "Payment canceled.", Toast.LENGTH_SHORT).show();
        } else if (result instanceof PaymentSheetResult.Failed) {
            String msg = ((PaymentSheetResult.Failed) result).getError().getLocalizedMessage();
            Toast.makeText(this, "Payment failed: " + msg, Toast.LENGTH_LONG).show();
        }
    }
}
