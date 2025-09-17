package my.foodon.pizzamania.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.content.res.ColorStateList;
import android.graphics.Color;

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
import java.util.Set;

import my.foodon.pizzamania.R;
import my.foodon.pizzamania.adapters.PizzaAdapter;
import my.foodon.pizzamania.cart.FirebaseCart;
import my.foodon.pizzamania.fav.FirebaseFavorites;
import my.foodon.pizzamania.models.Pizza;

public class MenuFragment extends Fragment {

    private RecyclerView recyclerView;
    private PizzaAdapter adapter;
    private final List<Pizza> pizzaList = new ArrayList<>();
    private ChipGroup chipGroup;

    private DatabaseReference menuRef;
    private String selectedCategory = "All";

    // Favorites observer
    private ValueEventListener favListener;
    private String uid;

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

        // Firebase DB ref -> branches/{branch}/menuitems
        String path = my.foodon.pizzamania.BranchSession.branchPath(requireContext(), "menuitems");
        menuRef = FirebaseDatabase.getInstance().getReference(path);

        setupMaterialChips();

        // Resolve uid for favorites + cart
        try {
            uid = FirebaseCart.requireUid();
        } catch (Exception e) {
            uid = null;
        }

        // Build adapter WITH uid so that favorite toggles work
        adapter = new PizzaAdapter(getContext(), pizzaList, (pizza, size) -> {
            String message = pizza.getName() + (size != null ? " (" + size.getDisplayName() + ")" : "") + " added to cart!";
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            addToCart(pizza, size);
        }, uid);
        recyclerView.setAdapter(adapter);

        // Observe favorites to keep hearts in sync
        if (uid != null) {
            favListener = FirebaseFavorites.observeIds(uid, new FirebaseFavorites.FavSetListener() {
                @Override public void onChanged(Set<String> ids) { adapter.setFavorites(ids); }
                @Override public void onError(String msg) {
                    Toast.makeText(getContext(), "Fav error: " + msg, Toast.LENGTH_SHORT).show();
                }
            });
        }

        listenForMenuChanges(); // realtime updates
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (favListener != null && uid != null) {
            FirebaseFavorites.removeObserver(uid, favListener);
        }
    }

    private void listenForMenuChanges() {
        menuRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pizzaList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Pizza p = child.getValue(Pizza.class);
                    if (p != null) {
                        if (p.getId() == null || p.getId().isEmpty()) p.setId(child.getKey());
                        // If imageUrl is missing in some records, keep as null; adapter handles placeholder
                        // Ensure your DB field names exactly match model getters/setters (imageUrl, smallPrice, ...)
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
        chipGroup.setSingleSelection(true);

        for (String cat : categories) {
            Chip chip = new Chip(requireContext(), null,
                    com.google.android.material.R.style.Widget_Material3_Chip_Filter);
            chip.setText(cat);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(true);
            chip.setEnsureMinTouchTargetSize(true);
            chip.setClickable(true);

            ColorStateList backgroundColors = new ColorStateList(
                    new int[][]{ new int[]{android.R.attr.state_checked}, new int[]{} },
                    new int[]{ Color.parseColor("#fc3503"), Color.parseColor("#F5F5F5") }
            );
            ColorStateList textColors = new ColorStateList(
                    new int[][]{ new int[]{android.R.attr.state_checked}, new int[]{} },
                    new int[]{ Color.WHITE, Color.parseColor("#757575") }
            );
            ColorStateList checkedIconColors = new ColorStateList(
                    new int[][]{ new int[]{android.R.attr.state_checked}, new int[]{} },
                    new int[]{ Color.WHITE, Color.parseColor("#757575") }
            );
            chip.setChipBackgroundColor(backgroundColors);
            chip.setTextColor(textColors);
            chip.setCheckedIconTint(checkedIconColors);

            if ("All".equalsIgnoreCase(cat)) chip.setChecked(true);

            chip.setOnClickListener(v -> {
                selectedCategory = cat;
                adapter.filterByCategory(cat);
            });
            chipGroup.addView(chip);
        }

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> { });
    }

    private void addToCart(Pizza pizza, Pizza.PizzaSize size) {
        try {
            String uid = FirebaseCart.requireUid();

            String sizeCode;
            String sizeLabel;
            switch (size) {
                case SMALL:  sizeCode = "S"; sizeLabel = "Small";  break;
                case LARGE:  sizeCode = "L"; sizeLabel = "Large";  break;
                default:     sizeCode = "M"; sizeLabel = "Medium"; break;
            }

            double price = pizza.getPrice(size);
            String imageUrl = pizza.getImageUrl();
            String pizzaId = pizza.getId();

            if (pizzaId == null || pizzaId.isEmpty()) {
                // FIX: use proper regex escape for whitespace
                pizzaId = pizza.getName().replaceAll("\\s+", "_").toLowerCase();
            }

            FirebaseCart.upsert(uid, pizzaId, pizza.getName(), sizeCode, sizeLabel, imageUrl, price);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Cart error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public PizzaAdapter getAdapter() {
        return adapter;
    }
}
