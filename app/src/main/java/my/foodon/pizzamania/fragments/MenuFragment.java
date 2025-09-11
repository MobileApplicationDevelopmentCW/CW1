package my.foodon.pizzamania.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import my.foodon.pizzamania.R;
import my.foodon.pizzamania.adapters.PizzaAdapter;
import my.foodon.pizzamania.cart.CartManager;
import my.foodon.pizzamania.models.Pizza;

public class MenuFragment extends Fragment {

    private RecyclerView recyclerView;
    private PizzaAdapter adapter;
    private final List<Pizza> pizzaList = new ArrayList<>();
    private ChipGroup chipGroup;

    private DatabaseReference menuRef;
    private String selectedCategory = "All";

    // Categories shown as Material Filter Chips
    private final String[] categories = {"All", "Pizza", "Burger", "Drinks", "Dessert"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        chipGroup = view.findViewById(R.id.chipGroup);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Firebase DB ref -> /menuitems
        menuRef = FirebaseDatabase.getInstance().getReference("menuitems"); // Realtime DB path [10]

        setupMaterialChips();

        adapter = new PizzaAdapter(getContext(), pizzaList, (pizza, size) -> {
            String message = pizza.getName() + (size != null ? " (" + size.getDisplayName() + ")" : "") + " added to cart!";
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            addToCart(pizza, size);
        });
        recyclerView.setAdapter(adapter);

        listenForMenuChanges(); // realtime updates [10][11]
        return view;
    }

    private void listenForMenuChanges() {
        menuRef.addValueEventListener(new ValueEventListener() { // realtime listener [10][11]
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pizzaList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Pizza p = child.getValue(Pizza.class); // requires no-arg ctor and getters/setters [10]
                    if (p != null) {
                        if (p.getId() == null || p.getId().isEmpty()) {
                            p.setId(child.getKey());
                        }
                        if (!p.isInStock()) continue; // optional stock filter
                        pizzaList.add(p);
                    }
                }
                // Re-apply current category
                adapter.filterByCategory(selectedCategory);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load menu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupMaterialChips() {
        chipGroup.removeAllViews();
        chipGroup.setSingleSelection(true); // one active category at a time [3]

        for (String cat : categories) {
            // Use Material 3 Filter Chip style
            Chip chip = new Chip(requireContext(), null,
                    com.google.android.material.R.style.Widget_Material3_Chip_Filter);
            chip.setText(cat);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(true);
            chip.setEnsureMinTouchTargetSize(true); // 48dp touch target [8]
            chip.setClickable(true);

            if ("All".equalsIgnoreCase(cat)) {
                chip.setChecked(true);
            }

            chip.setOnClickListener(v -> {
                selectedCategory = cat;
                adapter.filterByCategory(cat);
            });

            chipGroup.addView(chip);
        }

        // Optional: central listener if you prefer reacting to selection state changes
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // not needed since each chip's click already filters
        });
    }

    private void addToCart(Pizza pizza, Pizza.PizzaSize size) {
        CartManager.getInstance().addToCart(pizza, size);
    }

    public PizzaAdapter getAdapter() {
        return adapter;
    }
}
