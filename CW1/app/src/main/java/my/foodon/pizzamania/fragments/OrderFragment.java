package my.foodon.pizzamania.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import my.foodon.pizzamania.R;
import my.foodon.pizzamania.models.Order;

public class OrderFragment extends Fragment {

    private static final String TAG = "OrderFragment";

    private RecyclerView recyclerViewOrders;
    private OrdersRecyclerAdapter ordersAdapter;
    private ProgressBar progressBar;
    private TextView textViewNoOrders;

    private List<Order> ordersList;
    private DatabaseReference branchesRef;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;

    public OrderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);

        initializeViews(view);
        setupFirebase();
        setupRecyclerView();
        loadOrders();

        return view;
    }

    private void initializeViews(View view) {
        recyclerViewOrders = view.findViewById(R.id.recyclerViewOrders);
        progressBar = view.findViewById(R.id.progressBar);
        textViewNoOrders = view.findViewById(R.id.textViewNoOrders);
    }

    private void setupFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            currentUserId = firebaseAuth.getCurrentUser().getUid();
            branchesRef = FirebaseDatabase.getInstance().getReference("branches");
            Log.d(TAG, "Current User ID: " + currentUserId);
        } else {
            Log.e(TAG, "No authenticated user found!");
        }
    }

    private void setupRecyclerView() {
        ordersList = new ArrayList<>();
        ordersAdapter = new OrdersRecyclerAdapter(ordersList);

        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewOrders.setAdapter(ordersAdapter);
    }

    private void loadOrders() {
        if (currentUserId == null) {
            Log.e(TAG, "Current user ID is null");
            Toast.makeText(getContext(), "Please login to view orders", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        Log.d(TAG, "Starting to load active orders across branches for user: " + currentUserId);

        branchesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Total branches: " + dataSnapshot.getChildrenCount());
                ordersList.clear();

                for (DataSnapshot branchSnap : dataSnapshot.getChildren()) {
                    DataSnapshot ordersNode = branchSnap.child("orders").child(currentUserId);
                    for (DataSnapshot orderNode : ordersNode.getChildren()) {
                        List<Order> parsedOrders = parseOrderFromSnapshot(orderNode);
                        for (Order order : parsedOrders) {
                            if (currentUserId.equals(order.uid)) {
                                if (isActiveOrder(order.status)) {
                                    ordersList.add(order);
                                }
                            }
                        }
                    }
                }

                Log.d(TAG, "Total active user orders found: " + ordersList.size());

                // Sort orders by creation date (newest first)
                Collections.sort(ordersList, (o1, o2) ->
                        Long.compare(o2.createdAt, o1.createdAt));

                updateUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                showLoading(false);
                Toast.makeText(getContext(), "Failed to load orders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Collect all orders under this snapshot (supports nested structure)
     */
    private List<Order> parseOrderFromSnapshot(DataSnapshot orderSnapshot) {
        List<Order> orders = new ArrayList<>();
        String parentKey = orderSnapshot.getKey();
        Log.d(TAG, "=== Parsing order group: " + parentKey + " ===");

        try {
            if (orderSnapshot.hasChild("uid") && orderSnapshot.hasChild("amount")) {
                // Direct order structure
                Log.d(TAG, "Using direct order structure");
                Order order = parseDirectOrderStructure(orderSnapshot, parentKey);
                if (order != null) orders.add(order);
            } else {
                // Nested order structure
                Log.d(TAG, "Using nested order structure");
                for (DataSnapshot childSnapshot : orderSnapshot.getChildren()) {
                    if (childSnapshot.hasChild("uid") && childSnapshot.hasChild("amount")) {
                        // ✅ Use child key instead of parent key
                        Order order = parseDirectOrderStructure(childSnapshot, childSnapshot.getKey());
                        if (order != null) orders.add(order);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception parsing order: " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }

    private boolean isActiveOrder(String status) {
        if (status == null) return true;

        String lowerStatus = status.toLowerCase().trim();
        return !lowerStatus.equals("delivered") && !lowerStatus.equals("cancelled");
    }

    private Order parseDirectOrderStructure(DataSnapshot dataSnapshot, String orderId) {
        Order order = new Order();
        order.orderId = orderId; // ✅ Now always correct (child key if nested)

        // Parse fields safely
        if (dataSnapshot.child("amount").exists()) {
            Object amountObj = dataSnapshot.child("amount").getValue();
            try {
                if (amountObj instanceof Number) {
                    order.amount = ((Number) amountObj).doubleValue();
                } else if (amountObj instanceof String) {
                    order.amount = Double.parseDouble((String) amountObj);
                }
            } catch (Exception e) {
                Log.w(TAG, "Could not parse amount: " + amountObj);
            }
        }

        if (dataSnapshot.child("createdAt").exists()) {
            Object createdAtObj = dataSnapshot.child("createdAt").getValue();
            try {
                if (createdAtObj instanceof Number) {
                    order.createdAt = ((Number) createdAtObj).longValue();
                } else if (createdAtObj instanceof String) {
                    order.createdAt = Long.parseLong((String) createdAtObj);
                }
            } catch (Exception e) {
                Log.w(TAG, "Could not parse createdAt: " + createdAtObj);
                order.createdAt = System.currentTimeMillis();
            }
        } else {
            order.createdAt = System.currentTimeMillis();
        }

        order.customerAddress = dataSnapshot.child("customerAddress").getValue(String.class);
        order.customerName = dataSnapshot.child("customerName").getValue(String.class);
        order.customerPhone = dataSnapshot.child("customerPhone").getValue(String.class);
        order.paymentMethod = dataSnapshot.child("paymentMethod").getValue(String.class);
        order.status = dataSnapshot.child("status").getValue(String.class);
        order.uid = dataSnapshot.child("uid").getValue(String.class);

        // driver fields
        order.driverId = dataSnapshot.child("driverId").getValue(String.class);
        order.driverName = dataSnapshot.child("driverName").getValue(String.class);
        order.driverPlate = dataSnapshot.child("driverPlate").getValue(String.class);
        order.driverPhone = dataSnapshot.child("driverPhone").getValue(String.class);

        Boolean paidObj = dataSnapshot.child("paid").getValue(Boolean.class);
        order.paid = paidObj != null ? paidObj : false;

        // Parse items if they exist
        if (dataSnapshot.child("items").exists()) {
            try {
                order.items = new ArrayList<>();
                for (DataSnapshot itemSnapshot : dataSnapshot.child("items").getChildren()) {
                    Map<String, Object> item = (Map<String, Object>) itemSnapshot.getValue();
                    if (item != null) order.items.add(item);
                }
            } catch (Exception e) {
                Log.w(TAG, "Could not parse items for order: " + orderId);
                order.items = new ArrayList<>();
            }
        }

        return order;
    }

    private void updateUI() {
        showLoading(false);
        Log.d(TAG, "Updating UI with " + ordersList.size() + " active orders");

        if (ordersList.isEmpty()) {
            recyclerViewOrders.setVisibility(View.GONE);
            textViewNoOrders.setVisibility(View.VISIBLE);
            textViewNoOrders.setText("No active orders found");
        } else {
            recyclerViewOrders.setVisibility(View.VISIBLE);
            textViewNoOrders.setVisibility(View.GONE);
            ordersAdapter.notifyDataSetChanged();
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void setStatusColor(TextView textView, String status) {
        int backgroundColor;
        switch (status.toLowerCase()) {
            case "placed":
                backgroundColor = Color.parseColor("#FF9800");
                break;
            case "paid":
                backgroundColor = Color.parseColor("#8BC34A");
                break;
            case "preparing":
                backgroundColor = Color.parseColor("#2196F3");
                break;
            case "on_the_way":
                backgroundColor = Color.parseColor("#FF5722");
                break;
            case "delivered":
                backgroundColor = Color.parseColor("#4CAF50");
                break;
            case "cancelled":
                backgroundColor = Color.parseColor("#F44336");
                break;
            default:
                backgroundColor = Color.parseColor("#757575");
        }
        textView.setBackgroundColor(backgroundColor);
    }

    // RecyclerView Adapter
    private class OrdersRecyclerAdapter extends RecyclerView.Adapter<OrderViewHolder> {
        private final List<Order> orders;

        OrdersRecyclerAdapter(List<Order> orders) {
            this.orders = orders;
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_order, parent, false);
            return new OrderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            Order order = orders.get(position);

            holder.textOrderId.setText("Order #" + order.orderId.substring(0, Math.min(8, order.orderId.length())));
            holder.textCustomerName.setText("Customer: " + (order.customerName != null ? order.customerName : "N/A"));
            holder.textCustomerAddress.setText("Address: " + (order.customerAddress != null ? order.customerAddress : "N/A"));
            holder.textCustomerPhone.setText("Phone: " + (order.customerPhone != null ? order.customerPhone : "N/A"));
            holder.textOrderAmount.setText("₹" + (int) order.amount);
            holder.textPaymentMethod.setText(order.paymentMethod != null ? order.paymentMethod : "N/A");
            holder.textOrderDate.setText(formatDate(order.createdAt));

            String status = order.status != null ? order.status : "unknown";
            holder.textOrderStatus.setText(status.toUpperCase());
            setStatusColor(holder.textOrderStatus, status);

            if (order.paid) {
                holder.textPaymentStatus.setText(" • Paid");
                holder.textPaymentStatus.setTextColor(Color.parseColor("#4CAF50"));
            } else {
                holder.textPaymentStatus.setText(" • Not Paid");
                holder.textPaymentStatus.setTextColor(Color.parseColor("#FF5722"));
            }

            // bind driver
            if (order.driverName != null && !order.driverName.isEmpty()) {
                holder.textDriverName.setText("Driver: " + order.driverName);
                holder.textDriverPlate.setText("Plate: " + (order.driverPlate != null ? order.driverPlate : "-"));
                holder.textDriverPhone.setText("Tel: " + (order.driverPhone != null ? order.driverPhone : "-"));
            } else {
                holder.textDriverName.setText("Driver: Not assigned");
                holder.textDriverPlate.setText("Plate: -");
                holder.textDriverPhone.setText("Tel: -");
            }
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }
    }

    // ViewHolder Class
    private static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView textOrderId, textOrderStatus, textCustomerName, textCustomerAddress,
                textCustomerPhone, textOrderAmount, textPaymentMethod, textPaymentStatus,
                textOrderDate, textDriverName, textDriverPlate, textDriverPhone;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textOrderId = itemView.findViewById(R.id.textOrderId);
            textOrderStatus = itemView.findViewById(R.id.textOrderStatus);
            textCustomerName = itemView.findViewById(R.id.textCustomerName);
            textCustomerAddress = itemView.findViewById(R.id.textCustomerAddress);
            textCustomerPhone = itemView.findViewById(R.id.textCustomerPhone);
            textOrderAmount = itemView.findViewById(R.id.textOrderAmount);
            textPaymentMethod = itemView.findViewById(R.id.textPaymentMethod);
            textPaymentStatus = itemView.findViewById(R.id.textPaymentStatus);
            textOrderDate = itemView.findViewById(R.id.textOrderDate);
            textDriverName = itemView.findViewById(R.id.textDriverName);
            textDriverPlate = itemView.findViewById(R.id.textDriverPlate);
            textDriverPhone = itemView.findViewById(R.id.textDriverPhone);
        }
    }
}
