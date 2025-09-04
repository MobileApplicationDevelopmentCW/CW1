package my.foodon.pizzamania.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.core.view.GravityCompat;

import my.foodon.pizzamania.R;

public class HomeFragment extends Fragment {

    private ImageButton btnDrawer, btnSearch;
    private EditText searchBar;
    private ImageView logo;
    private boolean isSearching = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main_home, container, false);

        btnDrawer = view.findViewById(R.id.btnDrawer);
        btnSearch = view.findViewById(R.id.btnSearch);
        searchBar = view.findViewById(R.id.searchBar);
        logo = view.findViewById(R.id.logo);

        // Open drawer
        btnDrawer.setOnClickListener(v -> {
            DrawerLayout drawerLayout = requireActivity().findViewById(R.id.drawerLayout);
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Show search bar
        btnSearch.setOnClickListener(v -> {
            logo.setVisibility(View.GONE);
            searchBar.setVisibility(View.VISIBLE);
            searchBar.requestFocus();
            isSearching = true;
        });

        // Hide search bar when touching outside
        view.setOnTouchListener((v, event) -> {
            if (isSearching && event.getAction() == MotionEvent.ACTION_DOWN) {
                searchBar.clearFocus();
                searchBar.setVisibility(View.GONE);
                logo.setVisibility(View.VISIBLE);
                isSearching = false;
            }
            return false;
        });

        return view;
    }
}
