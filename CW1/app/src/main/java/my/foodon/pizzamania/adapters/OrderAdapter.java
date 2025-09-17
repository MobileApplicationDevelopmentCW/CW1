package my.foodon.pizzamania.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

import my.foodon.pizzamania.R;
import my.foodon.pizzamania.models.Order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private Context context;
    private DatabaseReference ordersRef;
    private List<my.foodon.pizzamania.models.Driver> drivers = new ArrayList<>();

    public OrderAdapter(List<Order> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
        String path = my.foodon.pizzamania.BranchSession.branchPath(context, "orders");
        ordersRef = FirebaseDatabase.getInstance().getReference(path);
    }

    public void setDrivers(List<my.foodon.pizzamania.models.Driver> drivers) {
        this.drivers.clear();
        if (drivers != null) this.drivers.addAll(drivers);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.tvOrderId.setText("Order ID: " + order.orderId);
        holder.tvCustomer.setText("Customer: " + order.customerName);
        holder.tvAmount.setText("Amount: $" + order.amount);

        // Concatenate item names
        StringBuilder itemsStr = new StringBuilder();
        if (order.items != null) {
            for (Map<String, Object> item : order.items) {
                if (itemsStr.length() > 0) {
                    itemsStr.append(", ");
                }
                itemsStr.append(item.get("name"));
            }
        }
        holder.tvItems.setText("Items: " + itemsStr.toString());

        // Driver spinner
        ArrayAdapter<String> driverAdapter = new ArrayAdapter<>(context,
                R.layout.spinner_item2, buildDriverLabels());
        driverAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item2);
        holder.spinnerDriver.setAdapter(driverAdapter);

        // preselect current assignment
        int driverPos = findDriverPosition(order.driverId);
        holder.spinnerDriver.setSelection(driverPos);

        // show assigned driver text
        if (order.driverName != null && !order.driverName.isEmpty()) {
            holder.tvAssignedDriver.setText("Driver: " + order.driverName + " (" + safe(order.driverPlate) + ") - " + safe(order.driverPhone));
        } else {
            holder.tvAssignedDriver.setText("Driver: Not assigned");
        }

        // Status spinner setup
        String[] statusOptions = {"placed", "paid", "preparing", "on_the_way", "delivered", "cancelled"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                R.layout.spinner_item2, statusOptions);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item2);
        holder.spinnerStatus.setAdapter(adapter);

        // Set spinner selection to current status
        int statusPos = adapter.getPosition(order.status != null ? order.status : "placed");
        holder.spinnerStatus.setSelection(statusPos);

        // Set color for the selected status
        setStatusColor(holder.spinnerStatus, order.status);

        // Handle status change
        holder.spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean firstCall = true; // avoid triggering on initial setup

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (firstCall) {
                    firstCall = false;
                    return;
                }

                String newStatus = statusOptions[pos];
                order.status = newStatus;

                // Update color
                setStatusColor(holder.spinnerStatus, newStatus);

                // ✅ Build map for partial update
                Map<String, Object> updates = new HashMap<>();
                updates.put("status", newStatus);
                updates.put("updatedAt", System.currentTimeMillis()); // optional field

                // ✅ Correct Firebase path (scoped by branch + userId + orderId)
                ordersRef.child(order.uid)
                        .child(order.orderId)
                        .updateChildren(updates)
                        .addOnSuccessListener(aVoid -> Toast.makeText(context,
                                "Status updated to " + newStatus, Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(context,
                                "Failed to update status: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        holder.spinnerDriver.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean first = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (first) { first = false; return; }
                if (position == 0) {
                    // none selected -> clear assignment
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("driverId", null);
                    updates.put("driverName", null);
                    updates.put("driverPlate", null);
                    updates.put("driverPhone", null);
                    persistDriver(order, updates, "Driver cleared");
                    holder.tvAssignedDriver.setText("Driver: Not assigned");
                    return;
                }

                my.foodon.pizzamania.models.Driver d = drivers.get(position - 1);
                Map<String, Object> updates = new HashMap<>();
                updates.put("driverId", d.did);
                updates.put("driverName", d.dname);
                updates.put("driverPlate", d.dplate);
                // prefer dtel if available
                String phone = d.dtel != null ? d.dtel : null;
                updates.put("driverPhone", phone);
                persistDriver(order, updates, "Driver assigned: " + d.dname);
                holder.tvAssignedDriver.setText("Driver: " + d.dname + " (" + safe(d.dplate) + ") - " + safe(phone));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    // Helper method to color status
    private void setStatusColor(Spinner spinner, String status) {
        TextView selectedView = (TextView) spinner.getSelectedView();
        if (selectedView == null) return;

        switch (status) {
            case "placed":
                selectedView.setTextColor(Color.parseColor("#FF9800")); // orange
                break;
            case "paid":
                selectedView.setTextColor(Color.parseColor("#2196F3")); // blue
                break;
            case "preparing":
                selectedView.setTextColor(Color.parseColor("#FF9800")); // orange
                break;
            case "on_the_way":
                selectedView.setTextColor(Color.parseColor("#03A9F4")); // light blue
                break;
            case "delivered":
                selectedView.setTextColor(Color.parseColor("#4CAF50")); // green
                break;
            case "cancelled":
                selectedView.setTextColor(Color.parseColor("#F44336")); // red
                break;
            default:
                selectedView.setTextColor(Color.BLACK);
        }
    }

    private List<String> buildDriverLabels() {
        List<String> labels = new ArrayList<>();
        labels.add("Select driver...");
        for (my.foodon.pizzamania.models.Driver d : drivers) {
            labels.add(d.dname + " (" + safe(d.dplate) + ")");
        }
        return labels;
    }

    private int findDriverPosition(String driverId) {
        if (driverId == null) return 0;
        for (int i = 0; i < drivers.size(); i++) {
            if (driverId.equals(drivers.get(i).did)) return i + 1; // +1 due to hint item
        }
        return 0;
    }

    private String safe(String s) { return s == null ? "" : s; }

    private void persistDriver(Order order, Map<String, Object> updates, String successMsg) {
        ordersRef.child(order.uid)
                .child(order.orderId)
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvCustomer, tvAmount, tvItems, tvAssignedDriver;
        Spinner spinnerStatus, spinnerDriver;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvCustomer = itemView.findViewById(R.id.tvCustomer);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvItems = itemView.findViewById(R.id.tvItems);
            spinnerStatus = itemView.findViewById(R.id.spinnerStatus);
            spinnerDriver = itemView.findViewById(R.id.spinnerDriver);
            tvAssignedDriver = itemView.findViewById(R.id.tvAssignedDriver);
        }
    }
}
