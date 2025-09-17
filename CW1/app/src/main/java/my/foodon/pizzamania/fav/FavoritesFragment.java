package my.foodon.pizzamania.fav;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import my.foodon.pizzamania.MainActivity;
import my.foodon.pizzamania.R;
import my.foodon.pizzamania.adapters.PizzaAdapter;
import my.foodon.pizzamania.cart.FirebaseCart;
import my.foodon.pizzamania.fragments.HomeFragment;
import my.foodon.pizzamania.models.Pizza;
import java.util.concurrent.atomic.AtomicInteger;

public class FavoritesFragment extends Fragment {

    private RecyclerView rv;
    private TextView txtEmpty;
    private PizzaAdapter adapter;
    private DatabaseReference menuRef;       // branch-scoped menu path
    private ValueEventListener favListener;  // favorites observer
    private String uid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = inf.inflate(R.layout.fragment_favorites, c, false);

        // Back button -> Home
        ImageView backBtn = v.findViewById(R.id.backBtn);
        if (backBtn != null) {
            backBtn.setOnClickListener(v1 -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).switchFragment(new HomeFragment());
                }
            });
        }

        rv = v.findViewById(R.id.rvFav);
        txtEmpty = v.findViewById(R.id.txtEmpty);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Resolve uid (required for favorites toggle and cart)
        try { uid = FirebaseCart.requireUid(); } catch (Exception e) { uid = null; }

        // Adapter with uid so heart toggles also work from this screen
        adapter = new PizzaAdapter(getContext(), new ArrayList<>(), (pizza, size) -> {
            try {
                String uidLocal = FirebaseCart.requireUid();
                Pizza.PizzaSize chosen = Pizza.PizzaSize.MEDIUM; // quick-add default
                String sizeCode = "M";
                String sizeLabel = "Medium";
                double price = pizza.getPrice(chosen);
                FirebaseCart.upsert(uidLocal, pizza.getId(), pizza.getName(), sizeCode, sizeLabel, pizza.getImageUrl(), price);
                Toast.makeText(getContext(), "Added to cart", Toast.LENGTH_SHORT).show();
            } catch (Exception e1) {
                Toast.makeText(getContext(), e1.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, uid);
        rv.setAdapter(adapter);

        // BRANCH-SCOPED menu reference: branches/{nearestBranch}/menuitems
        String branchMenuPath = my.foodon.pizzamania.BranchSession.branchPath(requireContext(), "menuitems");
        menuRef = FirebaseDatabase.getInstance().getReference(branchMenuPath);

        subscribeFavorites();
        return v;
    }

    private void subscribeFavorites() {
        if (uid == null) {
            // Not logged in; show empty
            adapter.replaceAll(Collections.emptyList());
            txtEmpty.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
            return;
        }

        favListener = FirebaseFavorites.observeIds(uid, new FirebaseFavorites.FavSetListener() {
            @Override public void onChanged(Set<String> ids) {
                if (ids == null || ids.isEmpty()) {
                    adapter.replaceAll(Collections.emptyList());
                    txtEmpty.setVisibility(View.VISIBLE);
                    rv.setVisibility(View.GONE);
                } else {
                    loadMenusByIds(ids);
                }
            }
            @Override public void onError(String msg) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMenusByIds(Set<String> ids) {
        List<Pizza> out = new ArrayList<>();
        final int total = ids.size();
        final AtomicInteger done = new AtomicInteger(0); // mutable counter captured by inner class

        txtEmpty.setVisibility(View.GONE);
        rv.setVisibility(View.VISIBLE);

        for (String id : ids) {
            menuRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot s) {
                    Pizza p = s.getValue(Pizza.class);
                    if (p != null) {
                        if (p.getId() == null || p.getId().isEmpty()) p.setId(s.getKey());
                        if (p.isInStock()) out.add(p);
                    }
                    if (done.incrementAndGet() == total) adapter.replaceAll(out);
                }
                @Override public void onCancelled(@NonNull DatabaseError e) {
                    if (done.incrementAndGet() == total) adapter.replaceAll(out);
                }
            });
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (favListener != null && uid != null) {
            FirebaseFavorites.removeObserver(uid, favListener);
        }
    }
}
