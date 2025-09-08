package my.foodon.pizzamania.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import my.foodon.pizzamania.R;
import my.foodon.pizzamania.models.MenuItem;
import my.foodon.pizzamania.models.Pizza;

public class PizzaAdapter extends RecyclerView.Adapter<PizzaAdapter.PizzaViewHolder> {

    private Context context;
    private List<Pizza> pizzaList;       // original list
    private List<Pizza> filteredList;    // filtered list
    private OnPizzaClickListener listener;
    private DecimalFormat priceFormat;



    public PizzaAdapter(Context context, List<Pizza> pizzaList, OnPizzaClickListener listener) {
        this.context = context;
        this.pizzaList = pizzaList;
        this.listener = listener;
        this.priceFormat = new DecimalFormat("#,##0");

        //initialize filteredList with all pizzas first
        this.filteredList = new ArrayList<>(pizzaList);
    }

    @NonNull
    @Override
    public PizzaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pizza, parent, false);
        return new PizzaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PizzaViewHolder holder, int position) {
        //bind filtered list, not original
        Pizza pizza = filteredList.get(position);

        holder.pizzaName.setText(pizza.getName());
        holder.pizzaDescription.setText(pizza.getDescription());
        holder.pizzaImage.setImageResource(pizza.getImageResource());

        // Default Medium
        holder.radioMedium.setChecked(true);
        updatePriceDisplay(holder, pizza, Pizza.PizzaSize.MEDIUM);

        // Size change
        holder.sizeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Pizza.PizzaSize selectedSize = Pizza.PizzaSize.MEDIUM;
            if (checkedId == R.id.radioSmall) {
                selectedSize = Pizza.PizzaSize.SMALL;
            } else if (checkedId == R.id.radioLarge) {
                selectedSize = Pizza.PizzaSize.LARGE;
            }
            updatePriceDisplay(holder, pizza, selectedSize);
        });

        // Add to cart
        holder.addToCartBtn.setOnClickListener(v -> {
            if (listener != null) {
                Pizza.PizzaSize selectedSize = getSelectedSize(holder.sizeRadioGroup);
                listener.onAddToCart(pizza, selectedSize);
            }
        });
    }

    @Override
    public int getItemCount() {
        // use filteredList size
        return filteredList != null ? filteredList.size() : 0;
    }

    // filtering method
    public void filter(String query) {
        filteredList.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(pizzaList); // reset
        } else {
            String lower = query.toLowerCase();
            for (Pizza pizza : pizzaList) {
                if (pizza.getName().toLowerCase().contains(lower) ||
                        pizza.getDescription().toLowerCase().contains(lower)) {
                    filteredList.add(pizza);
                }
            }
        }
        notifyDataSetChanged();
    }

    // Helpers
    private void updatePriceDisplay(PizzaViewHolder holder, Pizza pizza, Pizza.PizzaSize size) {
        double price = pizza.getPrice(size);
        holder.pizzaPrice.setText("LKR " + priceFormat.format(price));
        holder.sizePriceHint.setText("(" + size.getDisplayName() + ")");
    }

    private Pizza.PizzaSize getSelectedSize(RadioGroup radioGroup) {
        int checkedId = radioGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.radioSmall) return Pizza.PizzaSize.SMALL;
        if (checkedId == R.id.radioLarge) return Pizza.PizzaSize.LARGE;
        return Pizza.PizzaSize.MEDIUM;
    }

    // ViewHolder
    public static class PizzaViewHolder extends RecyclerView.ViewHolder {
        ImageView pizzaImage;
        TextView pizzaName, pizzaDescription, pizzaPrice, sizePriceHint;
        RadioGroup sizeRadioGroup;
        RadioButton radioSmall, radioMedium, radioLarge;
        Button addToCartBtn;

        public PizzaViewHolder(@NonNull View itemView) {
            super(itemView);
            pizzaImage = itemView.findViewById(R.id.pizzaImage);
            pizzaName = itemView.findViewById(R.id.pizzaName);
            pizzaDescription = itemView.findViewById(R.id.pizzaDescription);
            pizzaPrice = itemView.findViewById(R.id.pizzaPrice);
            sizePriceHint = itemView.findViewById(R.id.sizePriceHint);
            sizeRadioGroup = itemView.findViewById(R.id.sizeRadioGroup);
            radioSmall = itemView.findViewById(R.id.radioSmall);
            radioMedium = itemView.findViewById(R.id.radioMedium);
            radioLarge = itemView.findViewById(R.id.radioLarge);
            addToCartBtn = itemView.findViewById(R.id.addToCartBtn);
        }
    }

    // Listener
    public interface OnPizzaClickListener {
        void onAddToCart(Pizza pizza, Pizza.PizzaSize size);
    }
}

