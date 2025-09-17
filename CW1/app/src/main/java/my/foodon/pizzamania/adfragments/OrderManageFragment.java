package my.foodon.pizzamania.adfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import my.foodon.pizzamania.AdminActivity;
import my.foodon.pizzamania.R;
import my.foodon.pizzamania.adapters.OrderAdapter;
import my.foodon.pizzamania.models.Driver;
import my.foodon.pizzamania.models.Order;

public class OrderManageFragment extends Fragment {

    private RecyclerView rvOrders;
    private Spinner spinnerFilterStatus;
    private ImageView ivMenu;
    private OrderAdapter orderAdapter;
    private List<Order> allOrders;  // Keep all orders for filtering
    private List<Order> orderList;  // Orders currently displayed
    private DatabaseReference ordersRef;
    private DatabaseReference driversRef;
    private List<Driver> driverList = new ArrayList<>();

    public OrderManageFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_manage, container, false);

        // Initialize views
        rvOrders = view.findViewById(R.id.rvOrders);
        spinnerFilterStatus = view.findViewById(R.id.spinnerFilterStatus);
        ivMenu = view.findViewById(R.id.ivMenu);

        // RecyclerView setup
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize lists
        allOrders = new ArrayList<>();
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList, getContext());
        rvOrders.setAdapter(orderAdapter);

        // Menu button click listener
        ivMenu.setOnClickListener(v -> {
            if (getActivity() instanceof AdminActivity) {
                ((AdminActivity) getActivity()).openDrawer();
            }
        });

        // Setup filter spinner
        String[] filterOptions = {"All", "placed", "paid", "preparing", "on_the_way", "delivered", "cancelled"};
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(getContext(),
                R.layout.spinner_item_white_bg, filterOptions);
        filterAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_white_bg);
        spinnerFilterStatus.setAdapter(filterAdapter);

        spinnerFilterStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedStatus = filterOptions[pos];
                filterOrders(selectedStatus);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Firebase references
        String ordersPath = my.foodon.pizzamania.BranchSession.branchPath(requireContext(), "orders");
        ordersRef = FirebaseDatabase.getInstance().getReference(ordersPath);
        driversRef = FirebaseDatabase.getInstance().getReference("drivers");

        // Load drivers by current branch, then orders
        loadDriversThenOrders();

        return view;
    }

    private void loadDriversThenOrders() {
        String currentBranch = my.foodon.pizzamania.BranchSession.getBranch(requireContext());
        driversRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                driverList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Driver d = ds.getValue(Driver.class);
                    if (d != null && currentBranch.equals(d.dbranch)) {
                        driverList.add(d);
                    }
                }
                orderAdapter.setDrivers(driverList);
                loadOrdersFromFirebase();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load drivers: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                loadOrdersFromFirebase();
            }
        });
    }

    private void loadOrdersFromFirebase() {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allOrders.clear();
                orderList.clear();

                // Loop through each user UID
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    // Loop through each order for this user
                    for (DataSnapshot orderSnapshot : userSnapshot.getChildren()) {
                        Order order = orderSnapshot.getValue(Order.class);
                        if (order != null) {
                            allOrders.add(order);
                            orderList.add(order); // initially show all
                        }
                    }
                }

                orderAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load orders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Filter orders based on selected status
    private void filterOrders(String status) {
        orderList.clear();
        if (status.equals("All")) {
            orderList.addAll(allOrders);
        } else {
            for (Order order : allOrders) {
                if (order.status != null && order.status.equals(status)) {
                    orderList.add(order);
                }
            }
        }
        orderAdapter.notifyDataSetChanged();
    }
}