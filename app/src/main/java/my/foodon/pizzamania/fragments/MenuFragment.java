package my.foodon.pizzamania.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import my.foodon.pizzamania.R;
import my.foodon.pizzamania.adapters.PizzaAdapter;
import my.foodon.pizzamania.models.Pizza;

public class MenuFragment extends Fragment {

    private RecyclerView recyclerView;
    private PizzaAdapter adapter;
    private List<Pizza> pizzaList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        initializeViews(view);
        setupRecyclerView();
        loadPizzaData();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupRecyclerView() {
        // Create pizza list
        pizzaList = new ArrayList<>();
        loadPizzaData();

        // In MenuFragment.java, update the adapter initialization:

        adapter = new PizzaAdapter(getContext(), pizzaList, (pizza, size) -> {
            // Handle Add to Cart click with size
            String message = pizza.getName() + " (" + size.getDisplayName() + ") added to cart!";
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

            // TODO: Add to cart with size information
            addToCart(pizza, size);
        });


        recyclerView.setAdapter(adapter);
    }

    private void loadPizzaData() {
        // Clear existing data
        pizzaList.clear();

        // Add sample pizzas with Sri Lankan pricing
        pizzaList.add(new Pizza("Margherita", "Classic cheese & tomato", 1200, R.drawable.margherita));
        pizzaList.add(new Pizza("Pepperoni", "Spicy pepperoni with cheese", 1500, R.drawable.pepperoni));
        pizzaList.add(new Pizza("Veggie Delight", "Fresh vegetables & herbs", 1300, R.drawable.veggie));
        pizzaList.add(new Pizza("BBQ Chicken", "Grilled chicken with BBQ sauce", 1800, R.drawable.bbq_chicken));
        pizzaList.add(new Pizza("Hawaiian", "Ham, pineapple & cheese", 1600, R.drawable.hawaiian));

        // Notify adapter of data changes
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public PizzaAdapter getAdapter() {
        return adapter;
    }


    private void addToCart(Pizza pizza, Pizza.PizzaSize size) {
        // TODO: Implement cart functionality
        // You can integrate with Firebase here
        // CartManager.getInstance().addPizza(pizza);
    }
}
