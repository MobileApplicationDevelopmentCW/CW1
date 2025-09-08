package my.foodon.pizzamania;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
    import android.widget.ArrayAdapter;
    import android.widget.Button;
    import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class AddItemActivity extends AppCompatActivity {

    private ImageView imagePreview;
    private EditText etName, etDescription, etSmallPrice, etMediumPrice, etLargePrice;
    private Spinner spinnerCategory;
    private CheckBox cbInStock;
    private Button btnAddItem;

    private Uri selectedImageUri; // store selected image

    // Register an ActivityResultLauncher for selecting an image
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imagePreview.setImageURI(uri); // Display image in ImageView
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_menu_item);

        imagePreview = findViewById(R.id.imagePreview);
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        etSmallPrice = findViewById(R.id.etSmallPrice);
        etMediumPrice = findViewById(R.id.etMediumPrice);
        etLargePrice = findViewById(R.id.etLargePrice);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        cbInStock = findViewById(R.id.cbInStock);
        btnAddItem = findViewById(R.id.btnAddItem);

        // Setup Spinner with dummy data
        String[] categories = {"Pizza", "Burger", "Drinks", "Dessert"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(adapter);

        // When ImageView is clicked, open gallery picker
        imagePreview.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnAddItem.setOnClickListener(v -> handleAddItem());
    }

    private void handleAddItem() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String smallPrice = etSmallPrice.getText().toString().trim();
        String mediumPrice = etMediumPrice.getText().toString().trim();
        String largePrice = etLargePrice.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        boolean inStock = cbInStock.isChecked();

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show preview of collected data
        Toast.makeText(this,
                "Item Added: " + name +
                        "\nCategory: " + category +
                        "\nSmall: " + smallPrice +
                        "\nMedium: " + mediumPrice +
                        "\nLarge: " + largePrice +
                        "\nIn Stock: " + (inStock ? "Yes" : "No") +
                        (selectedImageUri != null ? "\nImage Selected ✅" : "\nNo Image Selected"),
                Toast.LENGTH_LONG).show();

        // TODO: Send this data and selectedImageUri to backend or database
    }
}
