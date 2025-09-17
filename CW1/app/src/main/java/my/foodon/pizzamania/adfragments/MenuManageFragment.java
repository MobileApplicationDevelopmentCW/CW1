package my.foodon.pizzamania.adfragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import my.foodon.pizzamania.AddItemActivity;
import my.foodon.pizzamania.R;
import my.foodon.pizzamania.adapters.MenuManageAdapter;
import my.foodon.pizzamania.models.Pizza;

public class MenuManageFragment extends Fragment {

    private RecyclerView rvMenu;
    private MenuManageAdapter adapter;
    private DatabaseReference menuRef;

    // Optional: simple paging state (disabled by default; keep for future)
    private static final int PAGE_SIZE = 30;
    private String lastKey = null;
    private boolean isLoading = false;
    private boolean endReached = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_menu_manage, container, false);

        rvMenu = v.findViewById(R.id.rvMenu);
        rvMenu.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MenuManageAdapter(new MenuManageAdapter.Listener() {
            @Override public void onUpdate(Pizza item) {
                Intent i = new Intent(requireContext(), AddItemActivity.class);
                i.putExtra("mode", "edit");
                i.putExtra("id", item.getId());
                i.putExtra("name", item.getName());
                i.putExtra("desc", item.getDescription());
                i.putExtra("cat", item.getCategory());
                i.putExtra("sp", item.getSmallPrice());
                i.putExtra("mp", item.getMediumPrice());
                i.putExtra("lp", item.getLargePrice());
                i.putExtra("inStock", item.isInStock());
                i.putExtra("imageUrl", item.getImageUrl());
                startActivity(i);
            }

            @Override public void onDelete(Pizza item) {
                if (item.getId() == null || item.getId().isEmpty()) {
                    Toast.makeText(getContext(), "Missing item id", Toast.LENGTH_SHORT).show();
                    return;
                }
                showConfirmDelete(requireContext(), item);
            }

            @Override public void onStockToggle(Pizza item, boolean inStock) {
                if (item.getId() == null || item.getId().isEmpty()) return;
                menuRef.child(item.getId()).child("inStock").setValue(inStock);
            }
        });
        rvMenu.setAdapter(adapter);

        Button btnOpenAdd = v.findViewById(R.id.btnOpenAddItem);
        btnOpenAdd.setOnClickListener(view -> {
            Intent i = new Intent(requireContext(), AddItemActivity.class);
            i.putExtra("mode", "add");
            startActivity(i);
        });

        String path = my.foodon.pizzamania.BranchSession.branchPath(requireContext(), "menuitems");
        menuRef = FirebaseDatabase.getInstance().getReference(path);

        // Realtime full feed (simple and reliable for admin)
        listenMenuRealtime();

        // Optional: enable lazy paging instead of realtime if data grows very large
        // setupPagingScroll();
        // loadFirstPage();

        return v;
    }

    private void showConfirmDelete(Context ctx, Pizza item) {
        new AlertDialog.Builder(ctx)
                .setTitle("Delete menu item")
                .setMessage("Are you sure you want to delete \"" + item.getName() + "\"?")
                .setPositiveButton("Delete", (d, w) -> {
                    menuRef.child(item.getId()).removeValue()
                            .addOnSuccessListener(v -> Toast.makeText(ctx, "Deleted", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(ctx, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setCancelable(true)
                .show();
    }

    // Realtime listener (recommended for admin management)
    private void listenMenuRealtime() {
        menuRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Pizza> list = new ArrayList<>();
                int count = 0;
                for (DataSnapshot child : snapshot.getChildren()) {
                    Pizza p = child.getValue(Pizza.class);
                    if (p != null) {
                        if (p.getId() == null || p.getId().isEmpty()) p.setId(child.getKey());
                        list.add(p);
                        count++;
                    }
                }
                Toast.makeText(getContext(), "Loaded " + count + " items", Toast.LENGTH_SHORT).show();
                adapter.setItems(list);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Load failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ========== Optional: Paging hooks (use instead of listenMenuRealtime if needed) ==========

    private void loadFirstPage() {
        isLoading = true;
        Query q = menuRef.orderByKey().limitToFirst(PAGE_SIZE);
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Pizza> page = new ArrayList<>();
                String last = null;
                for (DataSnapshot c : snapshot.getChildren()) {
                    last = c.getKey();
                    Pizza p = c.getValue(Pizza.class);
                    if (p != null) { if (p.getId()==null) p.setId(c.getKey()); page.add(p); }
                }
                lastKey = last;
                endReached = page.size() < PAGE_SIZE;
                adapter.setItems(page);
                isLoading = false;
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { isLoading = false; }
        });
    }

    private void loadNextPage() {
        if (isLoading || endReached || lastKey == null) return;
        isLoading = true;
        Query q = menuRef.orderByKey().startAt(lastKey).limitToFirst(PAGE_SIZE + 1); // include lastKey, skip 1
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Pizza> page = new ArrayList<>();
                boolean skipped = false;
                String last = null;
                for (DataSnapshot c : snapshot.getChildren()) {
                    if (!skipped) { skipped = true; continue; } // skip duplicate lastKey
                    last = c.getKey();
                    Pizza p = c.getValue(Pizza.class);
                    if (p != null) { if (p.getId()==null) p.setId(c.getKey()); page.add(p); }
                }
                if (page.isEmpty()) {
                    endReached = true;
                } else {
                    lastKey = last;
                    endReached = page.size() < PAGE_SIZE;
                    // append to current list
                    List<Pizza> merged = new ArrayList<>(adapter.getItems());
                    merged.addAll(page);
                    adapter.setItems(merged);
                }
                isLoading = false;
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { isLoading = false; }
        });
    }

    private void setupPagingScroll() {
        rvMenu.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                if (lm == null) return;
                int lastVisible = lm.findLastVisibleItemPosition();
                int total = adapter.getItemCount();
                if (total - lastVisible <= 5) loadNextPage(); // prefetch when near end
            }
        });
    }
}
