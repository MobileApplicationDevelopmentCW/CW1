package my.foodon.pizzamania.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import my.foodon.pizzamania.R;
import my.foodon.pizzamania.models.Pizza;

public class PizzaAdapter extends RecyclerView.Adapter<PizzaAdapter.PizzaViewHolder> {

    private final Context context;
    private final List<Pizza> pizzaList;    // master list (all pizzas from Firebase)
    private final List<Pizza> filteredList; // currently displayed (after category/search)
    private final OnPizzaClickListener listener;
    private final DecimalFormat priceFormat;

    public PizzaAdapter(Context context, List<Pizza> pizzaList, OnPizzaClickListener listener) {
        this.context = context;
        this.pizzaList = pizzaList;
        this.listener = listener;
        this.priceFormat = new DecimalFormat("#,##0.00");
        this.filteredList = new ArrayList<>(pizzaList);
    }

    // Call this after the backing pizzaList has changed (e.g., Firebase listener updated it)
    public void refreshKeepingFilter(String currentCategory) {
        filterByCategory(currentCategory);
    }

    @NonNull
    @Override
    public PizzaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pizza, parent, false);
        return new PizzaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PizzaViewHolder holder, int position) {
        Pizza pizza = filteredList.get(position);

        holder.pizzaName.setText(pizza.getName());
        holder.pizzaDescription.setText(pizza.getDescription());

        // Image: prefer URL from Firebase; fallback to local resource or placeholder
        if (!TextUtils.isEmpty(pizza.getImageUrl())) {
            Glide.with(context)
                    .load(pizza.getImageUrl())
                    .placeholder(R.drawable.ic_pizza_placeholder)
                    .error(R.drawable.ic_pizza_placeholder)
                    .into(holder.pizzaImage); // Glide recommended for RecyclerView [5][6]
        } else {
            int res = pizza.getImageResource() != 0
                    ? pizza.getImageResource()
                    : R.drawable.ic_pizza_placeholder;
            holder.pizzaImage.setImageResource(res);
        }

        // Default size Medium and price
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
        return filteredList != null ? filteredList.size() : 0;
    }

    // Category filtering
    public void filterByCategory(String category) {
        filteredList.clear();
        if (TextUtils.isEmpty(category) || "All".equalsIgnoreCase(category)) {
            filteredList.addAll(pizzaList);
        } else {
            for (Pizza pizza : pizzaList) {
                if (pizza.getCategory() != null &&
                        pizza.getCategory().equalsIgnoreCase(category)) {
                    filteredList.add(pizza);
                }
            }
        }
        notifyDataSetChanged();
    }

    // Optional: search filtering by name/description
    public void filter(String query) {
        filteredList.clear();
        if (TextUtils.isEmpty(query)) {
            filteredList.addAll(pizzaList);
        } else {
            String lower = query.toLowerCase();
            for (Pizza pizza : pizzaList) {
                String n = pizza.getName() != null ? pizza.getName().toLowerCase() : "";
                String d = pizza.getDescription() != null ? pizza.getDescription().toLowerCase() : "";
                if (n.contains(lower) || d.contains(lower)) {
                    filteredList.add(pizza);
                }
            }
        }
        notifyDataSetChanged();
    }

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

    public static class PizzaViewHolder extends RecyclerView.ViewHolder {
        ImageView pizzaImage;
        TextView pizzaName, pizzaDescription, pizzaPrice, sizePriceHint;
        RadioGroup sizeRadioGroup;
        RadioButton radioSmall, radioMedium, radioLarge;
        ImageButton addToCartBtn;

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

    // Listener for “Add to Cart”
    public interface OnPizzaClickListener {
        void onAddToCart(Pizza pizza, Pizza.PizzaSize size);
    }
}
