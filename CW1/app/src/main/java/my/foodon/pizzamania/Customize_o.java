package my.foodon.pizzamania;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;
import java.util.List;

public class Customize_o extends AppCompatActivity {
    private static final double BASE_PRICE = 12.99;
    private double currentPrice = BASE_PRICE;

    private Spinner crustSpinner, sizeSpinner, sauceSpinner, cheeseSpinner;
    private CheckBox onionsCheckBox, bellPeppersCheckBox, mushroomsCheckBox, olivesCheckBox,
            cornCheckBox, jalapenosCheckBox, spinachCheckBox, pepperoniCheckBox,
            chickenCheckBox, baconCheckBox, sausageCheckBox;
    private Button addToCartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.customize_o);

        // Initialize all views
        initializeViews();

        // Set up spinners with data
        setupSpinners();

        // Set up event listeners
        setupListeners();

        // Insets handling
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            );
            return insets;
        });
    }

    private void initializeViews() {
        crustSpinner = findViewById(R.id.crustSpinner);
        sizeSpinner = findViewById(R.id.sizeSpinner);
        sauceSpinner = findViewById(R.id.sauceSpinner);
        cheeseSpinner = findViewById(R.id.cheeseSpinner);

        onionsCheckBox = findViewById(R.id.onionsCheckBox);
        bellPeppersCheckBox = findViewById(R.id.bellPeppersCheckBox);
        mushroomsCheckBox = findViewById(R.id.mushroomsCheckBox);
        olivesCheckBox = findViewById(R.id.olivesCheckBox);
        cornCheckBox = findViewById(R.id.cornCheckBox);
        jalapenosCheckBox = findViewById(R.id.jalapenosCheckBox);
        spinachCheckBox = findViewById(R.id.spinachCheckBox);

        pepperoniCheckBox = findViewById(R.id.pepperoniCheckBox);
        chickenCheckBox = findViewById(R.id.chickenCheckBox);
        baconCheckBox = findViewById(R.id.baconCheckBox);
        sausageCheckBox = findViewById(R.id.sausageCheckBox);

        addToCartButton = findViewById(R.id.addToCartButton);
    }

    private void setupSpinners() {
        // Crust options
        String[] crustOptions = {"Thin Crust", "Thick Crust", "Cheese Burst", "Whole Wheat", "Gluten-Free"};
        ArrayAdapter<String> crustAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_black_text, crustOptions);
        crustAdapter.setDropDownViewResource(R.layout.spinner_item_black_text);
        crustSpinner.setAdapter(crustAdapter);

        // Size options
        String[] sizeOptions = {"Small", "Medium", "Large", "Extra-Large"};
        ArrayAdapter<String> sizeAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_black_text, sizeOptions);
        sizeAdapter.setDropDownViewResource(R.layout.spinner_item_black_text);
        sizeSpinner.setAdapter(sizeAdapter);
        sizeSpinner.setSelection(1); // Medium as default

        // Sauce options
        String[] sauceOptions = {"Tomato Classic", "Barbecue", "Alfredo / White Sauce", "Pesto", "Spicy Marinara"};
        ArrayAdapter<String> sauceAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_black_text, sauceOptions);
        sauceAdapter.setDropDownViewResource(R.layout.spinner_item_black_text);
        sauceSpinner.setAdapter(sauceAdapter);

        // Cheese options
        String[] cheeseOptions = {"Mozzarella", "Cheddar", "Parmesan", "Vegan Cheese", "Extra Cheese"};
        ArrayAdapter<String> cheeseAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_black_text, cheeseOptions);
        cheeseAdapter.setDropDownViewResource(R.layout.spinner_item_black_text);
        cheeseSpinner.setAdapter(cheeseAdapter);
    }

    private void setupListeners() {
        // Spinner changes
        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatePizzaPrice();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        crustSpinner.setOnItemSelectedListener(spinnerListener);
        sizeSpinner.setOnItemSelectedListener(spinnerListener);
        cheeseSpinner.setOnItemSelectedListener(spinnerListener);

        // Checkbox changes
        CompoundButton.OnCheckedChangeListener checkboxListener = (buttonView, isChecked) -> updatePizzaPrice();

        onionsCheckBox.setOnCheckedChangeListener(checkboxListener);
        bellPeppersCheckBox.setOnCheckedChangeListener(checkboxListener);
        mushroomsCheckBox.setOnCheckedChangeListener(checkboxListener);
        olivesCheckBox.setOnCheckedChangeListener(checkboxListener);
        cornCheckBox.setOnCheckedChangeListener(checkboxListener);
        jalapenosCheckBox.setOnCheckedChangeListener(checkboxListener);
        spinachCheckBox.setOnCheckedChangeListener(checkboxListener);
        pepperoniCheckBox.setOnCheckedChangeListener(checkboxListener);
        chickenCheckBox.setOnCheckedChangeListener(checkboxListener);
        baconCheckBox.setOnCheckedChangeListener(checkboxListener);
        sausageCheckBox.setOnCheckedChangeListener(checkboxListener);

        addToCartButton.setOnClickListener(v -> addPizzaToCart());
    }

    private void updatePizzaPrice() {
        currentPrice = BASE_PRICE;

        // Size
        String size = sizeSpinner.getSelectedItem().toString();
        switch (size) {
            case "Small":
                currentPrice = 10.99;
                break;
            case "Medium":
                currentPrice = 12.99;
                break;
            case "Large":
                currentPrice = 15.99;
                break;
            case "Extra-Large":
                currentPrice = 18.99;
                break;
        }

        // Crust
        String crust = crustSpinner.getSelectedItem().toString();
        if (crust.equals("Cheese Burst")) {
            currentPrice += 2.00;
        } else if (crust.equals("Gluten-Free")) {
            currentPrice += 1.50;
        }

        // Cheese
        String cheese = cheeseSpinner.getSelectedItem().toString();
        if (cheese.equals("Extra Cheese")) {
            currentPrice += 1.50;
        }

        // Toppings
        int toppingCount = getSelectedToppingCount();
        currentPrice += (toppingCount * 0.75);

        String formattedPrice = String.format("%.2f", currentPrice);
        addToCartButton.setText(getString(R.string.add_to_cart_format, formattedPrice));
    }

    private int getSelectedToppingCount() {
        int count = 0;
        if (onionsCheckBox.isChecked()) count++;
        if (bellPeppersCheckBox.isChecked()) count++;
        if (mushroomsCheckBox.isChecked()) count++;
        if (olivesCheckBox.isChecked()) count++;
        if (cornCheckBox.isChecked()) count++;
        if (jalapenosCheckBox.isChecked()) count++;
        if (spinachCheckBox.isChecked()) count++;
        if (pepperoniCheckBox.isChecked()) count++;
        if (chickenCheckBox.isChecked()) count++;
        if (baconCheckBox.isChecked()) count++;
        if (sausageCheckBox.isChecked()) count++;
        return count;
    }

    private void addPizzaToCart() {
        Pizza pizza = new Pizza();
        pizza.crust = crustSpinner.getSelectedItem().toString();
        pizza.size = sizeSpinner.getSelectedItem().toString();
        pizza.sauce = sauceSpinner.getSelectedItem().toString();
        pizza.cheese = cheeseSpinner.getSelectedItem().toString();
        pizza.price = currentPrice;

        List<String> toppings = new ArrayList<>();
        if (onionsCheckBox.isChecked()) toppings.add("Onions");
        if (bellPeppersCheckBox.isChecked()) toppings.add("Bell Peppers");
        if (mushroomsCheckBox.isChecked()) toppings.add("Mushrooms");
        if (olivesCheckBox.isChecked()) toppings.add("Olives");
        if (cornCheckBox.isChecked()) toppings.add("Corn");
        if (jalapenosCheckBox.isChecked()) toppings.add("Jalapeños");
        if (spinachCheckBox.isChecked()) toppings.add("Spinach");
        if (pepperoniCheckBox.isChecked()) toppings.add("Pepperoni");
        if (chickenCheckBox.isChecked()) toppings.add("Chicken");
        if (baconCheckBox.isChecked()) toppings.add("Bacon");
        if (sausageCheckBox.isChecked()) toppings.add("Sausage");
        pizza.toppings = toppings;

        try {
            String uid = my.foodon.pizzamania.auth.AuthGuard.requireUidOrRedirect(this);

            // Construct pizza name
            String name = pizza.crust + " Pizza with " + pizza.cheese;
            if (!pizza.toppings.isEmpty()) {
                name += " + " + String.join(", ", pizza.toppings);
            }

            // Unique pizza ID and size code
            String pizzaId = "custom_" + System.currentTimeMillis();
            String sizeCode = pizza.size.toLowerCase();

            // ✅ Corrected argument order
            my.foodon.pizzamania.cart.FirebaseCart.addOrIncrement(
                    uid,
                    pizzaId,
                    sizeCode,
                    name,
                    pizza.size,   // size label
                    null,         // imageUrl (optional)
                    pizza.price   // price
            );

            Toast.makeText(this, "Pizza added to cart", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show();
        }
    }


    // --------------------- Pizza Class -------------------
    public static class Pizza {
        public String crust;
        public String size;
        public String sauce;
        public String cheese;
        public List<String> toppings = new ArrayList<>();
        public double price;

        public double getPrice() {
            return price;
        }
    }

    // --------------------- Cart Manager -------------------
    public static class CartManager {
        private static final List<Pizza> cartItems = new ArrayList<>();

        public static void addToCart(Pizza pizza) {
            cartItems.add(pizza);
        }

        public static List<Pizza> getCartItems() {
            return new ArrayList<>(cartItems);
        }

        public static double getTotalPrice() {
            double total = 0;
            for (Pizza p : cartItems) {
                total += p.price;
            }
            return total;
        }

        public static void clearCart() {
            cartItems.clear();
        }

        public static int getItemCount() {
            return cartItems.size();
        }
    }
}