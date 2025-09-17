package my.foodon.pizzamania.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import my.foodon.pizzamania.R;
import my.foodon.pizzamania.models.Pizza;

public class MenuManageAdapter extends RecyclerView.Adapter<MenuManageAdapter.VH> {

    public int getItems() {
        return 0;
    }

    public interface Listener {
        void onUpdate(Pizza item);
        void onDelete(Pizza item);
        void onStockToggle(Pizza item, boolean inStock);
    }

    private final List<Pizza> data = new ArrayList<>();
    private final Listener listener;

    public MenuManageAdapter(Listener l) { this.listener = l; setHasStableIds(true); }

    public void setItems(List<Pizza> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    @Override public long getItemId(int position) {
        String id = data.get(position).getId();
        return id != null ? id.hashCode() : data.get(position).hashCode();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu_manage, parent, false);
        return new VH(row);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        h.bind(data.get(position));
    }

    @Override public int getItemCount() { return data.size(); }

    class VH extends RecyclerView.ViewHolder {
        ImageView imgItem;
        TextView txtName, txtCategory, txtPrices;
        CheckBox cbStock;
        MaterialButton btnUpdate, btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            imgItem = itemView.findViewById(R.id.imgItem);
            txtName = itemView.findViewById(R.id.txtName);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            txtPrices = itemView.findViewById(R.id.txtPrices);
            cbStock = itemView.findViewById(R.id.cbStock);
            btnUpdate = itemView.findViewById(R.id.btnUpdate);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            btnUpdate.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                listener.onUpdate(data.get(pos));
            });
            btnDelete.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                listener.onDelete(data.get(pos));
            });
            cbStock.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int pos = getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                listener.onStockToggle(data.get(pos), isChecked);
            });
        }

        void bind(Pizza p) {
            txtName.setText(p.getName());
            txtCategory.setText(p.getCategory());
            String prices = "S: " + p.getSmallPrice() + " | M: " + p.getMediumPrice() + " | L: " + p.getLargePrice();
            txtPrices.setText(prices);
            cbStock.setChecked(p.isInStock());
            String url = p.getImageUrl();
            if (url != null && !url.isEmpty()) {
                Glide.with(imgItem.getContext())
                        .load(url)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(imgItem);
            } else {
                imgItem.setImageResource(R.drawable.placeholder);
            }
        }
    }
}