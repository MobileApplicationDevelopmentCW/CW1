package my.foodon.pizzamania;

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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AddItemActivity extends AppCompatActivity {

    private ImageView imagePreview;
    private EditText etName, etDescription, etSmallPrice, etMediumPrice, etLargePrice;
    private Spinner spinnerCategory;
    private CheckBox cbInStock;
    private Button btnAddItem;

    private Uri selectedImageUri;

    private DatabaseReference databaseRef;
    private StorageReference storageRef;

    // Image picker launcher
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imagePreview.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_menu_item);

        // Initialize views
        imagePreview = findViewById(R.id.imagePreview);
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        etSmallPrice = findViewById(R.id.etSmallPrice);
        etMediumPrice = findViewById(R.id.etMediumPrice);
        etLargePrice = findViewById(R.id.etLargePrice);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        cbInStock = findViewById(R.id.cbInStock);
        btnAddItem = findViewById(R.id.btnAddItem);

        // Firebase references
        databaseRef = FirebaseDatabase.getInstance().getReference("menuitems");
        storageRef = FirebaseStorage.getInstance().getReference("menuitem_images");

        // Spinner setup
        String[] categories = {"Pizza", "Burger", "Drinks", "Dessert"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item, // selected view
                categories
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);


        // Pick image
        imagePreview.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Add item
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

        // Upload image and save item
        uploadImageAndSaveItem(name, description, category,
                smallPrice, mediumPrice, largePrice, inStock);
    }

    private void uploadImageAndSaveItem(String name, String description, String category,
                                        String smallPrice, String mediumPrice, String largePrice,
                                        boolean inStock) {

        if (selectedImageUri != null) {
            // Create unique filename
            StorageReference imageRef = storageRef.child(System.currentTimeMillis() + ".jpg");

            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                saveMenuItemToDatabase(name, description, category,
                                        smallPrice, mediumPrice, largePrice, inStock, imageUrl);
                            }))
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show());
        } else {
            // Save without image
            saveMenuItemToDatabase(name, description, category,
                    smallPrice, mediumPrice, largePrice, inStock, null);
        }
    }

    private void saveMenuItemToDatabase(String name, String description, String category,
                                        String smallPrice, String mediumPrice, String largePrice,
                                        boolean inStock, String imageUrl) {

        String key = databaseRef.push().getKey(); // unique key
        MenuItem menuItem = new MenuItem(name, description, category,
                smallPrice, mediumPrice, largePrice, inStock, imageUrl);

        if (key != null) {
            databaseRef.child(key).setValue(menuItem)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Menu item added successfully ✅", Toast.LENGTH_SHORT).show();
                        finish(); // close activity
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to add item ❌", Toast.LENGTH_SHORT).show());
        }
    }

    // 🔹 Model class inside AddItemActivity
    public static class MenuItem {
        public String name, description, category;
        public String smallPrice, mediumPrice, largePrice;
        public boolean inStock;
        public String imageUrl;

        // Default constructor required for Firebase
        public MenuItem() {}

        public MenuItem(String name, String description, String category,
                        String smallPrice, String mediumPrice, String largePrice,
                        boolean inStock, String imageUrl) {
            this.name = name;
            this.description = description;
            this.category = category;
            this.smallPrice = smallPrice;
            this.mediumPrice = mediumPrice;
            this.largePrice = largePrice;
            this.inStock = inStock;
            this.imageUrl = imageUrl;
        }
    }
}

