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

import com.bumptech.glide.Glide;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import my.foodon.pizzamania.R;
import my.foodon.pizzamania.auth.AuthGuard;
import my.foodon.pizzamania.cart.CartRow;
import my.foodon.pizzamania.cart.FirebaseCart;

public class CartFragment extends Fragment {

    private RecyclerView cartRecyclerView;
    private TextView totalTxt;
    private final DecimalFormat money = new DecimalFormat("#,##0.00");

    private final List<CartRow> rows = new ArrayList<>();
    private CartAdapter cartAdapter;
    private ValueEventListener cartListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_cart, container, false);

        cartRecyclerView = v.findViewById(R.id.cartRecyclerView);
        totalTxt = v.findViewById(R.id.totalTxt);

        cartAdapter = new CartAdapter(rows);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cartRecyclerView.setAdapter(cartAdapter);

        View btnCheckout = v.findViewById(R.id.button);
        if (btnCheckout != null) {
            btnCheckout.setOnClickListener(view -> {
                if (rows.isEmpty()) {
                    Toast.makeText(getContext(), "Your cart is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                startActivity(new android.content.Intent(requireContext(),
                        my.foodon.pizzamania.checkout.CheckoutActivity.class));
            });
        }

        attachCartListener();
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        detachCartListener();
    }

    private void attachCartListener() {
        try {
            String uid = AuthGuard.requireUidOrRedirect(requireActivity());
            cartListener = FirebaseCart.observeCart(uid, new FirebaseCart.CartListener() {
                @Override
                public void onCartChanged(List<CartRow> newRows) {
                    rows.clear();
                    rows.addAll(newRows);
                    cartAdapter.notifyDataSetChanged();
                    updateTotal();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(getContext(), "Cart load failed: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Not signed in", Toast.LENGTH_SHORT).show();
        }
    }

    private void detachCartListener() {
        try {
            if (cartListener != null) {
                String uid = FirebaseCart.requireUid();
                FirebaseCart.removeObserver(uid, cartListener);
            }
        } catch (Exception ignored) {}
    }

    private void updateTotal() {
        double total = 0.0;
        for (CartRow r : rows) {
            total += r.getUnitPrice() * r.getQuantity();
        }
        totalTxt.setText("Rs " + money.format(total));
    }

    // ---------------- RecyclerView Adapter ----------------
    private class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartVH> {

        private final List<CartRow> items;

        CartAdapter(List<CartRow> items) {
            this.items = items;
            setHasStableIds(true);
        }

        @Override
        public long getItemId(int position) {
            CartRow r = items.get(position);
            String key = FirebaseCart.itemKey(r.getPizzaId(), r.getSizeCode());
            return key.hashCode();
        }

        @NonNull
        @Override
        public CartVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View row = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.cart_item, parent, false);
            return new CartVH(row);
        }

        @Override
        public void onBindViewHolder(@NonNull CartVH holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class CartVH extends RecyclerView.ViewHolder {
            ImageButton removeBtn, minusBtn, plusBtn;
            TextView qty, name, unitPrice, totalPrice;
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

                removeBtn.setOnClickListener(v -> updateQuantity(-1));
                minusBtn.setOnClickListener(v -> updateQuantity(-1));
                plusBtn.setOnClickListener(v -> updateQuantity(1));
            }

            private void updateQuantity(int change) {
                int idx = getBindingAdapterPosition();
                if (idx == RecyclerView.NO_POSITION) return;
                CartRow r = items.get(idx);
                try {
                    String uid = FirebaseCart.requireUid();
                    if (change > 0) FirebaseCart.increment(uid, r.getPizzaId(), r.getSizeCode());
                    else FirebaseCart.decrement(uid, r.getPizzaId(), r.getSizeCode());
                } catch (Exception e) {
                    Toast.makeText(itemView.getContext(), "Not signed in", Toast.LENGTH_SHORT).show();
                }
            }

            void bind(CartRow r) {
                name.setText(r.name + " (" + r.sizeLabel + ")");
                unitPrice.setText("Rs " + money.format(r.unitPrice));
                qty.setText(String.valueOf(r.quantity));
                double line = r.unitPrice * r.quantity;
                totalPrice.setText("Rs " + money.format(line));

                if (r.imageUrl != null && !r.imageUrl.isEmpty()) {
                    // URL case
                    com.bumptech.glide.Glide.with(image.getContext())
                            .load(r.imageUrl)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .into(image);
                } else {
                    // Drawable fallback by name (optional)
                    image.setImageResource(resolveLocalDrawable(r.name));
                }
            }
            private int resolveLocalDrawable(String nameOrKey) {
                if (nameOrKey == null) return R.drawable.placeholder;
                String key = nameOrKey.toLowerCase().replaceAll("\\s+", "");
                switch (key) {
                    case "margherita": return R.drawable.margherita;
                    case "bbqpizza":   return R.drawable.bbq;
                    // add other known names here
                    default: return R.drawable.placeholder;
                }
            }
        }
    }
}