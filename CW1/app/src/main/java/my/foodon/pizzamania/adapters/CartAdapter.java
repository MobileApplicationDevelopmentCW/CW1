package my.foodon.pizzamania.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import my.foodon.pizzamania.R;
import my.foodon.pizzamania.cart.CartManager;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartVH> {

    public interface TotalUpdateListener { void onTotalUpdated(double newTotal); }

    private final List<CartManager.CartItem> items = new ArrayList<>();
    private final DecimalFormat money = new DecimalFormat("#,##0.00");
    private final TotalUpdateListener totalListener;

    public CartAdapter(List<CartManager.CartItem> initial, TotalUpdateListener listener) {
        if (initial != null) items.addAll(initial);
        this.totalListener = listener;
        setHasStableIds(true);
        notifyTotal();
    }

    @Override public long getItemId(int position) { return items.get(position).hashCode(); }

    public List<CartManager.CartItem> getItems() { return items; }

    @NonNull @Override
    public CartVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item, parent, false);
        RecyclerView rv = (RecyclerView) parent;
        if (rv.getItemAnimator() instanceof DefaultItemAnimator) {
            ((DefaultItemAnimator) rv.getItemAnimator()).setSupportsChangeAnimations(false);
        }
        return new CartVH(row);
    }

    @Override
    public void onBindViewHolder(@NonNull CartVH h, int position) {
        bindAll(h, items.get(position));
    }

    @Override
    public void onBindViewHolder(@NonNull CartVH h, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && "qtyOnly".equals(payloads.get(0))) {
            bindQtyAndTotals(h, items.get(position));
        } else {
            super.onBindViewHolder(h, position, payloads);
        }
    }

    private void bindAll(@NonNull CartVH h, CartManager.CartItem ci) {
        h.name.setText(ci.pizza.getName());
        h.unitPrice.setText("Rs " + money.format(ci.pizza.getPrice(ci.size)));
        if (h.imageView != null) h.imageView.setImageResource(ci.pizza.getImageResource());
        bindQtyAndTotals(h, ci);
    }

    private void bindQtyAndTotals(@NonNull CartVH h, CartManager.CartItem ci) {
        h.qty.setText(String.valueOf(ci.quantity));
        double lineTotal = ci.pizza.getPrice(ci.size) * ci.quantity;
        h.totalPrice.setText("Rs " + money.format(lineTotal));
    }

    @Override public int getItemCount() { return items.size(); }

    private void notifyTotal() {
        if (totalListener == null) return;
        double total = 0.0;
        for (CartManager.CartItem item : items) total += item.pizza.getPrice(item.size) * item.quantity;
        totalListener.onTotalUpdated(total);
    }

    private void handleRemove(int pos) {
        if (pos == RecyclerView.NO_POSITION) return;
        CartManager.CartItem ci = items.get(pos);
        CartManager.getInstance().removeFromCart(ci.pizza, ci.size);
        items.remove(pos);
        notifyItemRemoved(pos);
        notifyTotal();
    }

    private void handleMinus(int pos) {
        if (pos == RecyclerView.NO_POSITION) return;
        CartManager.CartItem ci = items.get(pos);
        if (ci.quantity > 1) {
            CartManager.getInstance().decrementQuantity(ci.pizza, ci.size);
            notifyItemChanged(pos, "qtyOnly");
        } else {
            CartManager.getInstance().removeFromCart(ci.pizza, ci.size);
            items.remove(pos);
            notifyItemRemoved(pos);
        }
        notifyTotal();
    }

    private void handlePlus(int pos) {
        if (pos == RecyclerView.NO_POSITION) return;
        CartManager.CartItem ci = items.get(pos);
        CartManager.getInstance().incrementQuantity(ci.pizza, ci.size);
        notifyItemChanged(pos, "qtyOnly");
        notifyTotal();
    }

    public class CartVH extends RecyclerView.ViewHolder {
        ImageButton removeBtn, minusBtn, plusBtn;
        TextView qty, name, unitPrice, totalPrice;
        ImageView imageView;

        public CartVH(@NonNull View itemView) {
            super(itemView);
            removeBtn = itemView.findViewById(R.id.removeBtn);
            minusBtn = itemView.findViewById(R.id.minusBtn);
            plusBtn  = itemView.findViewById(R.id.plusBtn);
            qty = itemView.findViewById(R.id.itemQty);
            name = itemView.findViewById(R.id.cartItemName);
            unitPrice = itemView.findViewById(R.id.cartItemUnitPrice);
            totalPrice = itemView.findViewById(R.id.cartItemTotalPrice);
            imageView = itemView.findViewById(R.id.cartItemImage);

            if (removeBtn != null) removeBtn.setOnClickListener(v -> handleRemove(getBindingAdapterPosition()));
            if (minusBtn  != null) minusBtn.setOnClickListener(v -> handleMinus(getBindingAdapterPosition()));
            if (plusBtn   != null) plusBtn.setOnClickListener(v -> handlePlus(getBindingAdapterPosition()));

            if (removeBtn != null) removeBtn.setContentDescription("Remove item");
            if (minusBtn  != null) minusBtn.setContentDescription("Decrease quantity");
            if (plusBtn   != null) plusBtn.setContentDescription("Increase quantity");

            int min = (int) (48 * itemView.getResources().getDisplayMetrics().density);
            if (removeBtn != null) { removeBtn.setMinimumWidth(min); removeBtn.setMinimumHeight(min); }
            if (minusBtn  != null) { minusBtn.setMinimumWidth(min);  minusBtn.setMinimumHeight(min); }
            if (plusBtn   != null) { plusBtn.setMinimumWidth(min);   plusBtn.setMinimumHeight(min); }
        }
    }
}
