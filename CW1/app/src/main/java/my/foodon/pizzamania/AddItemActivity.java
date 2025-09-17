package my.foodon.pizzamania;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
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

    // Mode/extras for edit
    private String mode = "add"; // "add" | "edit"
    private String itemId;
    private String existingImageUrl;

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

        // ✅ Change Status Bar Color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.black_background)); // #060606
        }

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

        // Firebase references (scoped by branch)
        String menuPath = BranchSession.branchPath(this, "menuitems");
        databaseRef = FirebaseDatabase.getInstance().getReference(menuPath);
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

        // Read mode + extras for edit
        if (getIntent() != null) {
            String m = getIntent().getStringExtra("mode");
            if (m != null) mode = m;
            if ("edit".equals(mode)) {
                itemId = getIntent().getStringExtra("id");
                String name = getIntent().getStringExtra("name");
                String desc = getIntent().getStringExtra("desc");
                String cat  = getIntent().getStringExtra("cat");
                String sp   = getIntent().getStringExtra("sp");
                String mp   = getIntent().getStringExtra("mp");
                String lp   = getIntent().getStringExtra("lp");
                boolean inS = getIntent().getBooleanExtra("inStock", true);
                existingImageUrl = getIntent().getStringExtra("imageUrl");

                if (name != null) etName.setText(name);
                if (desc != null) etDescription.setText(desc);
                if (sp != null)   etSmallPrice.setText(sp);
                if (mp != null)   etMediumPrice.setText(mp);
                if (lp != null)   etLargePrice.setText(lp);
                cbInStock.setChecked(inS);
                if (cat != null && spinnerCategory.getAdapter() != null) {
                    for (int i=0; i<spinnerCategory.getAdapter().getCount(); i++) {
                        if (cat.equalsIgnoreCase(String.valueOf(spinnerCategory.getAdapter().getItem(i)))) {
                            spinnerCategory.setSelection(i); break;
                        }
                    }
                }
                if (existingImageUrl != null && !existingImageUrl.isEmpty()) {
                    Glide.with(this)
                            .load(existingImageUrl)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .into(imagePreview);
                }
                btnAddItem.setText("Update Menu Item");
                setTitle("Edit Menu Item");
            } else {
                setTitle("Add Menu Item");
            }
        }

        // Pick image
        imagePreview.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Submit (add or update)
        btnAddItem.setOnClickListener(v -> handleSubmit());
    }

    private void handleSubmit() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String smallPrice = etSmallPrice.getText().toString().trim();
        String mediumPrice = etMediumPrice.getText().toString().trim();
        String largePrice = etLargePrice.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem() != null
                ? spinnerCategory.getSelectedItem().toString() : "";
        boolean inStock = cbInStock.isChecked();

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("edit".equals(mode)) {
            updateItemFlow(name, description, category, smallPrice, mediumPrice, largePrice, inStock);
        } else {
            addItemFlow(name, description, category, smallPrice, mediumPrice, largePrice, inStock);
        }
    }

    private void addItemFlow(String name, String description, String category,
                             String smallPrice, String mediumPrice, String largePrice,
                             boolean inStock) {
        if (selectedImageUri != null) {
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
            saveMenuItemToDatabase(name, description, category, smallPrice, mediumPrice, largePrice, inStock, null);
        }
    }

    private void updateItemFlow(String name, String description, String category,
                                String smallPrice, String mediumPrice, String largePrice,
                                boolean inStock) {
        if (itemId == null || itemId.isEmpty()) {
            Toast.makeText(this, "Missing item id", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedImageUri != null) {
            StorageReference imageRef = storageRef.child(System.currentTimeMillis() + ".jpg");
            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                updateMenuItemInDatabase(itemId, name, description, category,
                                        smallPrice, mediumPrice, largePrice, inStock, imageUrl);
                            }))
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show());
        } else {
            updateMenuItemInDatabase(itemId, name, description, category,
                    smallPrice, mediumPrice, largePrice, inStock, existingImageUrl);
        }
    }

    private void saveMenuItemToDatabase(String name, String description, String category,
                                        String smallPrice, String mediumPrice, String largePrice,
                                        boolean inStock, @Nullable String imageUrl) {
        String key = databaseRef.push().getKey(); // unique key
        MenuItem menuItem = new MenuItem(name, description, category,
                smallPrice, mediumPrice, largePrice, inStock, imageUrl);

        if (key != null) {
            databaseRef.child(key).setValue(menuItem)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Menu item added successfully ✅", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to add item ❌", Toast.LENGTH_SHORT).show());
        }
    }

    private void updateMenuItemInDatabase(String id, String name, String description, String category,
                                          String smallPrice, String mediumPrice, String largePrice,
                                          boolean inStock, @Nullable String imageUrl) {
        java.util.Map<String,Object> m = new java.util.HashMap<>();
        m.put("name", name);
        m.put("description", description);
        m.put("category", category);
        m.put("smallPrice", smallPrice);
        m.put("mediumPrice", mediumPrice);
        m.put("largePrice", largePrice);
        m.put("inStock", inStock);
        if (imageUrl != null) m.put("imageUrl", imageUrl);

        databaseRef.child(id).updateChildren(m)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Menu item updated ✅", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update item ❌", Toast.LENGTH_SHORT).show());
    }

    // Model class inside AddItemActivity (unchanged)
    public static class MenuItem {
        public String name, description, category;
        public String smallPrice, mediumPrice, largePrice;
        public boolean inStock;
        public String imageUrl;

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