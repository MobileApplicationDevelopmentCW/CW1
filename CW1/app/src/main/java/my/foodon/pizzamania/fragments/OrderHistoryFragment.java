package my.foodon.pizzamania.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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

import java.util.ArrayList;
import java.util.List;

import my.foodon.pizzamania.R;
import my.foodon.pizzamania.adapters.OrderHistoryAdapter;
import my.foodon.pizzamania.models.Order;

public class OrderHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private OrderHistoryAdapter orderAdapter;
    private List<Order> orderList;

    private DatabaseReference ordersRef;
    private String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewOrders);
        progressBar = view.findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        orderList = new ArrayList<>();
        orderAdapter = new OrderHistoryAdapter(orderList, getContext());
        recyclerView.setAdapter(orderAdapter);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (currentUserId == null) {
            Toast.makeText(getContext(), "You are not logged in!", Toast.LENGTH_SHORT).show();
            return view;
        }

        ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        fetchDeliveredOrders();

        return view;
    }

    private void fetchDeliveredOrders() {
        progressBar.setVisibility(View.VISIBLE);

        ordersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();

                for (DataSnapshot orderNode : snapshot.getChildren()) {
                    String orderKey = orderNode.getKey();

                    // Read order data
                    String status = orderNode.child("status").getValue(String.class);
                    String customerName = orderNode.child("customerName").getValue(String.class);
                    Double amount = orderNode.child("amount").getValue(Double.class);
                    String paymentMethod = orderNode.child("paymentMethod").getValue(String.class);
                    Boolean paid = orderNode.child("paid").getValue(Boolean.class);
                    Long createdAt = orderNode.child("createdAt").getValue(Long.class);
                    String customerPhone = orderNode.child("customerPhone").getValue(String.class);
                    String customerAddress = orderNode.child("customerAddress").getValue(String.class);

                    // Check if delivered
                    if (status != null && status.equalsIgnoreCase("delivered")) {
                        // Create Order object
                        Order order = new Order();
                        order.orderId = orderKey;
                        order.uid = currentUserId;
                        order.status = status;
                        order.customerName = customerName != null ? customerName : "";
                        order.amount = amount != null ? amount : 0.0;
                        order.paymentMethod = paymentMethod != null ? paymentMethod : "";
                        order.paid = paid != null ? paid : false;
                        order.createdAt = createdAt != null ? createdAt : 0L;
                        order.customerPhone = customerPhone != null ? customerPhone : "";
                        order.customerAddress = customerAddress != null ? customerAddress : "";

                        orderList.add(order);
                    }
                }

                orderAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                if (orderList.isEmpty()) {
                    Toast.makeText(getContext(), "No delivered orders found", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Found " + orderList.size() + " delivered orders", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to fetch orders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("OrderHistory", "Database error: " + error.getMessage());
            }
        });
    }
}