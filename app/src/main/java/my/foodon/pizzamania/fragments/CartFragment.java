package my.foodon.pizzamania.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import my.foodon.pizzamania.R;
import my.foodon.pizzamania.cart.CartManager;

public class CartFragment extends Fragment {

    private RecyclerView cartRecyclerView;
    private TextView totalTxt;
    private CartAdapter cartAdapter;
    private final DecimalFormat money = new DecimalFormat("#,##0.00");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_cart, container, false);

        cartRecyclerView = v.findViewById(R.id.cartRecyclerView);
        totalTxt = v.findViewById(R.id.totalTxt);

        cartRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cartAdapter = new CartAdapter(CartManager.getInstance().getCartItems());
        cartRecyclerView.setAdapter(cartAdapter);

        updateTotal();

        View btnCheckout = v.findViewById(R.id.button);
        if (btnCheckout != null) {
            btnCheckout.setOnClickListener(view -> {
                Toast.makeText(getContext(), "Proceeding to checkout", Toast.LENGTH_SHORT).show();
                // TODO: navigate to checkout
            });
        }

        return v;
    }

    private void updateTotal() {
        double total = 0.0;
        for (CartManager.CartItem item : cartAdapter.getItems()) {
            total += item.pizza.getPrice(item.size) * item.quantity;
        }
        totalTxt.setText("Rs " + money.format(total));
    }

    // Public call if another screen changes cart and returns
    public void refreshCart() {
        cartAdapter.replaceAll(CartManager.getInstance().getCartItems());
        updateTotal();
    }

    // Adapter for cart list
    private class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartVH> {

        private final List<CartManager.CartItem> items;
        CartAdapter(List<CartManager.CartItem> items) {
            this.items = new ArrayList<>(items); // local, mutable copy
        }

        List<CartManager.CartItem> getItems() {
            return items;
        }

        void replaceAll(List<CartManager.CartItem> newItems) {
            items.clear();
            items.addAll(newItems);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CartVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View row = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.cart_item, parent, false); // ensure name matches your XML
            return new CartVH(row);
        }

        @Override
        public void onBindViewHolder(@NonNull CartVH h, int position) {
            CartManager.CartItem ci = items.get(position);

            h.name.setText(ci.pizza.getName());
            h.unitPrice.setText("Rs " + money.format(ci.pizza.getPrice(ci.size)));
            h.qty.setText(String.valueOf(ci.quantity));
            h.image.setImageResource(ci.pizza.getImageResource());

            // line total
            double lineTotal = ci.pizza.getPrice(ci.size) * ci.quantity;
            h.totalPrice.setText("Rs " + money.format(lineTotal));

            // Remove entire line
            h.removeBtn.setOnClickListener(v -> {
                int idx = h.getBindingAdapterPosition();
                if (idx == RecyclerView.NO_POSITION) return;
                CartManager.getInstance().removeFromCart(ci.pizza, ci.size);
                items.remove(idx);
                notifyItemRemoved(idx);
                updateTotal();
            });

            // Decrement
            h.minusBtn.setOnClickListener(v -> {
                int idx = h.getBindingAdapterPosition();
                if (idx == RecyclerView.NO_POSITION) return;

                if (ci.quantity > 1) {
                    CartManager.getInstance().decrementQuantity(ci.pizza, ci.size);
                    ci.quantity--;
                    // update just the qty and total text
                    h.qty.setText(String.valueOf(ci.quantity));
                    h.totalPrice.setText("Rs " + money.format(ci.pizza.getPrice(ci.size) * ci.quantity));
                } else {
                    CartManager.getInstance().removeFromCart(ci.pizza, ci.size);
                    items.remove(idx);
                    notifyItemRemoved(idx);
                }
                updateTotal();
            });

            // Increment
            h.plusBtn.setOnClickListener(v -> {
                int idx = h.getBindingAdapterPosition();
                if (idx == RecyclerView.NO_POSITION) return;

                CartManager.getInstance().incrementQuantity(ci.pizza, ci.size);
                ci.quantity++;
                h.qty.setText(String.valueOf(ci.quantity));
                h.totalPrice.setText("Rs " + money.format(ci.pizza.getPrice(ci.size) * ci.quantity));
                updateTotal();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class CartVH extends RecyclerView.ViewHolder {
            ImageButton removeBtn;
            ImageButton minusBtn;
            ImageButton plusBtn;
            TextView qty;
            TextView name;
            TextView unitPrice;
            TextView totalPrice;
            ImageView image;

            CartVH(@NonNull View itemView) {
                super(itemView);
                removeBtn = itemView.findViewById(R.id.removeBtn);
                minusBtn = itemView.findViewById(R.id.minusBtn);
                plusBtn = itemView.findViewById(R.id.plusBtn);
                qty = itemView.findViewById(R.id.itemQty);
                name = itemView.findViewById(R.id.cartItemName);
                unitPrice = itemView.findViewById(R.id.cartItemUnitPrice);
                totalPrice = itemView.findViewById(R.id.cartItemTotalPrice);
                image = itemView.findViewById(R.id.cartItemImage);

                // Accessibility
                removeBtn.setContentDescription("Remove item");
                minusBtn.setContentDescription("Decrease quantity");
                plusBtn.setContentDescription("Increase quantity");

                int min = (int) (48 * itemView.getResources().getDisplayMetrics().density);
                removeBtn.setMinimumWidth(min); removeBtn.setMinimumHeight(min);
                minusBtn.setMinimumWidth(min);  minusBtn.setMinimumHeight(min);
                plusBtn.setMinimumWidth(min);   plusBtn.setMinimumHeight(min);
            }
        }
    }
}
