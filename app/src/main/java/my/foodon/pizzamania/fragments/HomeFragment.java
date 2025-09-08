package my.foodon.pizzamania.fragments;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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
    private MenuFragment menuFragment;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main_home, container, false);

        btnDrawer = view.findViewById(R.id.btnDrawer);
        btnSearch = view.findViewById(R.id.btnSearch);
        searchBar = view.findViewById(R.id.searchBar);
        logo = view.findViewById(R.id.logo);

        //Load MenuFragment inside the Home screen
        if (savedInstanceState == null) {
            menuFragment = new MenuFragment();
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.homeMenuContainer, menuFragment, "MENU_FRAGMENT")
                    .commit();
        } else {
            menuFragment = (MenuFragment) getChildFragmentManager().findFragmentByTag("MENU_FRAGMENT");
        }


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

        //Search text listener
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MenuFragment mf = (MenuFragment) getChildFragmentManager().findFragmentByTag("MENU_FRAGMENT");
                if (mf != null && mf.getAdapter() != null) {
                    mf.getAdapter().filter(s.toString());

                    //no pizza found show or hide
                    View root = getView();
                    if (root != null) {
                        TextView noResultsText = root.findViewById(R.id.noResultsText);
                        if (mf.getAdapter().getItemCount() == 0) {
                            noResultsText.setVisibility(View.VISIBLE);
                        } else {
                            noResultsText.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });

        // Hide search bar when touching outside
        view.setOnTouchListener((v, event) -> {
            if (isSearching && event.getAction() == MotionEvent.ACTION_DOWN) {
                searchBar.clearFocus();
                searchBar.setVisibility(View.GONE);
                logo.setVisibility(View.VISIBLE);
                hideKeyboard();
                isSearching = false;
            }
            return false;
        });

        return view;
    }
    private void hideKeyboard() {
        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager)
                            requireActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
