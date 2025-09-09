package my.foodon.pizzamania.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import my.foodon.pizzamania.R;
import my.foodon.pizzamania.adapters.PizzaAdapter;
import my.foodon.pizzamania.cart.CartManager;
import my.foodon.pizzamania.models.Pizza;

public class MenuFragment extends Fragment {

    private RecyclerView recyclerView;
    private PizzaAdapter adapter;
    private List<Pizza> pizzaList = new ArrayList<>();
    private LinearLayout chipGroup;

    private final String[] categories = {"All", "Classic", "Premium", "Vegetarian", "Chicken", "Drinks", "Sides"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        chipGroup = view.findViewById(R.id.chipGroup);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2-column grid

        loadPizzaData();
        setupCategoryChips();

        adapter = new PizzaAdapter(getContext(), pizzaList, (pizza, size) -> {
            String message = pizza.getName() + (size != null ? " (" + size.getDisplayName() + ")" : "") + " added to cart!";
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            addToCart(pizza, size);
        });

        recyclerView.setAdapter(adapter);

        // Show all by default
        adapter.filterByCategory("All");
        highlightFirstChip();

        return view;
    }

    private void highlightFirstChip() {
        if (chipGroup.getChildCount() > 0) {
            chipGroup.getChildAt(0).setAlpha(1f);
            // All others faded
            for (int i = 1; i < chipGroup.getChildCount(); i++) {
                chipGroup.getChildAt(i).setAlpha(0.6f);
            }
        }
    }

    private void loadPizzaData() {
        pizzaList.clear();

        pizzaList.add(new Pizza("Margherita", "Classic cheese & tomato", 1200, R.drawable.margherita, "Classic"));
        pizzaList.add(new Pizza("Pepperoni", "Spicy pepperoni with cheese", 1500, R.drawable.pepperoni, "Classic"));
        pizzaList.add(new Pizza("Veggie Delight", "Fresh vegetables & herbs", 1300, R.drawable.veggie, "Vegetarian"));
        pizzaList.add(new Pizza("BBQ Chicken", "Grilled chicken with BBQ sauce", 1800, R.drawable.bbq_chicken, "Chicken"));
        pizzaList.add(new Pizza("Hawaiian", "Ham, pineapple & cheese", 1600, R.drawable.hawaiian, "Premium"));
    }

    private void setupCategoryChips() {
        chipGroup.removeAllViews();
        for (String cat : categories) {
            Button chip = new Button(getContext());
            chip.setText(cat);
            chip.setBackgroundResource(R.drawable.chip_background); // Use a rounded bg shape
            chip.setTextColor(getResources().getColor(R.color.red));
            chip.setPadding(36, 10, 36, 10);
            chip.setTextSize(15);
            chip.setAllCaps(false);

            chip.setAlpha(cat.equals("All") ? 1f : 0.6f); // highlight "All" by default

            chip.setOnClickListener(v -> {
                adapter.filterByCategory(cat);
                highlightSelectedChip(chipGroup, chip);
            });
            chipGroup.addView(chip);
        }
    }

    private void highlightSelectedChip(LinearLayout chipGroup, Button selected) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View v = chipGroup.getChildAt(i);
            if (v instanceof Button) v.setAlpha(v == selected ? 1f : 0.6f);
        }
    }

    private void addToCart(Pizza pizza, Pizza.PizzaSize size) {
        // TODO: Implement cart logic (e.g., database or Firebase)
        CartManager.getInstance().addToCart(pizza, size);
        // You can show a Toast or update Cart badge here
    }

    public PizzaAdapter getAdapter() {
        return adapter;
    }


}
