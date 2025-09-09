package my.foodon.pizzamania.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import my.foodon.pizzamania.R;
import my.foodon.pizzamania.cart.CartManager;
import my.foodon.pizzamania.models.Pizza;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartVH> {

    public interface TotalUpdateListener {
        void onTotalUpdated(double newTotal);
    }

    private final List<CartManager.CartItem> items = new ArrayList<>();
    private final DecimalFormat money = new DecimalFormat("#,##0.00");
    private final TotalUpdateListener totalListener;

    public CartAdapter(List<CartManager.CartItem> initial, TotalUpdateListener listener) {
        if (initial != null) items.addAll(initial);
        this.totalListener = listener;
        notifyTotal();
    }

    public List<CartManager.CartItem> getItems() {
        return items;
    }

    @NonNull
    @Override
    public CartVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item, parent, false);
        return new CartVH(row);
    }

    @Override
    public void onBindViewHolder(@NonNull CartVH h, int position) {
        CartManager.CartItem ci = items.get(position);

        // Bind basic info
        h.name.setText(ci.pizza.getName());
        h.unitPrice.setText("Rs " + money.format(ci.pizza.getPrice(ci.size)));
        h.qty.setText(String.valueOf(ci.quantity));
        if (h.imageView != null) {
            h.imageView.setImageResource(ci.pizza.getImageResource());
        }

        double lineTotal = ci.pizza.getPrice(ci.size) * ci.quantity;
        h.totalPrice.setText("Rs " + money.format(lineTotal));

        // Remove whole line
        h.removeBtn.setOnClickListener(v -> {
            int idx = h.getBindingAdapterPosition();
            if (idx == RecyclerView.NO_POSITION) return;

            CartManager.getInstance().removeFromCart(ci.pizza, ci.size);
            items.remove(idx);
            notifyItemRemoved(idx);
            notifyTotal();
        });

        // Decrement quantity
        h.minusBtn.setOnClickListener(v -> {
            int idx = h.getBindingAdapterPosition();
            if (idx == RecyclerView.NO_POSITION) return;

            if (ci.quantity > 1) {
                CartManager.getInstance().decrementQuantity(ci.pizza, ci.size);
                ci.quantity--;
                notifyItemChanged(idx);
            } else {
                CartManager.getInstance().removeFromCart(ci.pizza, ci.size);
                items.remove(idx);
                notifyItemRemoved(idx);
            }
            notifyTotal();
        });

        // Increment quantity
        h.plusBtn.setOnClickListener(v -> {
            int idx = h.getBindingAdapterPosition();
            if (idx == RecyclerView.NO_POSITION) return;

            CartManager.getInstance().incrementQuantity(ci.pizza, ci.size);
            ci.quantity++;
            notifyItemChanged(idx);
            notifyTotal();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void notifyTotal() {
        if (totalListener == null) return;
        double total = 0.0;
        for (CartManager.CartItem item : items) {
            total += item.pizza.getPrice(item.size) * item.quantity;
        }
        totalListener.onTotalUpdated(total);
    }

    // ViewHolder
    public static class CartVH extends RecyclerView.ViewHolder {
        ImageButton removeBtn;
        ImageButton minusBtn;
        ImageButton plusBtn;
        TextView qty;
        TextView name;
        TextView unitPrice;
        TextView totalPrice;
        ImageView imageView;

        public CartVH(@NonNull View itemView) {
            super(itemView);
            removeBtn = itemView.findViewById(R.id.removeBtn);
            minusBtn = itemView.findViewById(R.id.minusBtn);
            plusBtn = itemView.findViewById(R.id.plusBtn);
            qty = itemView.findViewById(R.id.itemQty);
            name = itemView.findViewById(R.id.cartItemName);
            unitPrice = itemView.findViewById(R.id.cartItemUnitPrice);
            totalPrice = itemView.findViewById(R.id.cartItemTotalPrice);
            imageView = itemView.findViewById(R.id.cartItemImage);

            // Accessibility: labels
            if (removeBtn != null) removeBtn.setContentDescription("Remove item");
            if (minusBtn != null) minusBtn.setContentDescription("Decrease quantity");
            if (plusBtn != null) plusBtn.setContentDescription("Increase quantity");

            // Ensure minimum touch target (48dp)
            int min = (int) (48 * itemView.getResources().getDisplayMetrics().density);
            if (removeBtn != null) { removeBtn.setMinimumWidth(min); removeBtn.setMinimumHeight(min); }
            if (minusBtn != null) { minusBtn.setMinimumWidth(min); minusBtn.setMinimumHeight(min); }
            if (plusBtn != null) { plusBtn.setMinimumWidth(min); plusBtn.setMinimumHeight(min); }
        }
    }
}
