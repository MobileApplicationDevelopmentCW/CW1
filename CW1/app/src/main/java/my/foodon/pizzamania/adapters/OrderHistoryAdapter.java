package my.foodon.pizzamania.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import my.foodon.pizzamania.R;
import my.foodon.pizzamania.models.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private Context context;

    public OrderHistoryAdapter(List<Order> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_history_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        // Set Order ID
        holder.tvOrderId.setText("Order ID: " + (order.orderId != null ? order.orderId : "N/A"));

        // Set Customer Name
        holder.tvCustomer.setText("Customer: " + (order.customerName != null ? order.customerName : "N/A"));

        // Set Status (should be "Delivered" for order history)
        holder.tvStatus.setText("Status: Delivered");
        holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));

        // Set Amount
        holder.tvAmount.setText("Amount: $" + String.format("%.2f", order.amount));

        // Set Items: show only pizza names (comma-separated)
        Log.d("OrderHistoryAdapter", "Order items: " + (order.items != null ? order.items.size() : "null"));
        if (order.items != null && !order.items.isEmpty()) {
            StringBuilder itemsStr = new StringBuilder();
            for (Map<String, Object> item : order.items) {
                Log.d("OrderHistoryAdapter", "Item: " + item);
                Object name = item != null ? item.get("name") : null;
                if (name != null) {
                    if (itemsStr.length() > 0) itemsStr.append(", ");
                    itemsStr.append(String.valueOf(name));
                }
            }
            String itemsText = itemsStr.length() > 0 ? itemsStr.toString() : "No items";
            Log.d("OrderHistoryAdapter", "Items text: " + itemsText);
            holder.tvItems.setText(itemsText);
        } else {
            Log.d("OrderHistoryAdapter", "No items found, setting empty text");
            holder.tvItems.setText("No items");
        }

        // Set Order Date (if available)
        if (order.createdAt > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            String dateStr = sdf.format(new Date(order.createdAt));
            holder.tvOrderDate.setText("Ordered: " + dateStr);
            holder.tvOrderDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvOrderDate.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvCustomer, tvStatus, tvAmount, tvItems, tvOrderDate;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvCustomer = itemView.findViewById(R.id.tvCustomer);
            tvStatus = itemView.findViewById(R.id.spinnerStatus);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvItems = itemView.findViewById(R.id.tvItems);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
        }
    }
}