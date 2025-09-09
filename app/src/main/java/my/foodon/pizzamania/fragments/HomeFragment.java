package my.foodon.pizzamania.fragments;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;


import my.foodon.pizzamania.R;
import my.foodon.pizzamania.adapters.ImageSliderAdapter;

public class HomeFragment extends Fragment {

    private ImageButton btnDrawer;
    private EditText searchBar;
    private TextView txtLocation;
    private ViewPager2 imageSlider;
    private Handler sliderHandler = new Handler();
    private View rootView;
    private LinearLayout mainContainer;
    private boolean isKeyboardVisible = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main_home, container, false);
        rootView = view;

        btnDrawer = view.findViewById(R.id.btnDrawer);
        searchBar = view.findViewById(R.id.searchBar);
        txtLocation = view.findViewById(R.id.txtLocation);
        imageSlider = view.findViewById(R.id.imageSlider);

        // Set location (later you can fetch real GPS)
        txtLocation.setText("Colombo, Sri Lanka");

        // Setup search bar keyboard handling
        setupSearchBarKeyboard();

        // Setup keyboard visibility detection
        setupKeyboardVisibilityListener();

        // Auto Image Slider setup
        int[] images = {R.drawable.slide1, R.drawable.slide2, R.drawable.slide3};
        ImageSliderAdapter adapter = new ImageSliderAdapter(images);
        imageSlider.setAdapter(adapter);

        // Auto-slide every 3s
        Runnable sliderRunnable = new Runnable() {
            @Override
            public void run() {
                int nextItem = (imageSlider.getCurrentItem() + 1) % images.length;
                imageSlider.setCurrentItem(nextItem, true);
                sliderHandler.postDelayed(this, 5000);
            }
        };
        sliderHandler.postDelayed(sliderRunnable, 5000);

        // Drawer open
        btnDrawer.setOnClickListener(v -> {
            DrawerLayout drawerLayout = requireActivity().findViewById(R.id.drawerLayout);
            if (drawerLayout != null) {
                drawerLayout.openDrawer(androidx.core.view.GravityCompat.START);
            }
        });

        return view;
    }

    private void setupKeyboardVisibilityListener() {
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootView.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                if (keypadHeight > screenHeight * 0.15) { // Keyboard is visible
                    if (!isKeyboardVisible) {
                        isKeyboardVisible = true;
                        onKeyboardShown();
                    }
                } else { // Keyboard is hidden
                    if (isKeyboardVisible) {
                        isKeyboardVisible = false;
                        onKeyboardHidden();
                    }
                }
            }
        });
    }

    private void onKeyboardShown() {
        // Scroll the search bar into visible area when keyboard appears
        searchBar.post(() -> {
            int[] location = new int[2];
            searchBar.getLocationOnScreen(location);
            int searchBarTop = location[1];

            // Get status bar height
            int statusBarHeight = getStatusBarHeight();

            // If search bar is hidden behind status bar, scroll it into view
            if (searchBarTop < statusBarHeight + 20) { // 20dp buffer
                int scrollAmount = (statusBarHeight + 20) - searchBarTop;
                rootView.scrollBy(0, -scrollAmount);

                // Alternative: Adjust the entire layout
                rootView.setPadding(
                        rootView.getPaddingLeft(),
                        Math.max(statusBarHeight + 20, rootView.getPaddingTop()),
                        rootView.getPaddingRight(),
                        rootView.getPaddingBottom()
                );
            }
        });
    }

    private void onKeyboardHidden() {
        // Reset the layout padding when keyboard is hidden
        rootView.setPadding(0, 0, 0, 0);
        rootView.scrollTo(0, 0);
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void setupSearchBarKeyboard() {
        // Show keyboard when search bar is clicked/focused
        searchBar.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showKeyboard();
            }
        });

        searchBar.setOnClickListener(v -> {
            searchBar.requestFocus();
            showKeyboard();
        });

        // Hide keyboard when clicking outside the search bar
        rootView.setOnTouchListener((v, event) -> {
            if (searchBar.isFocused()) {
                searchBar.clearFocus();
                hideKeyboard();
            }
            return false; // Allow other touch events to be processed
        });

        // Also hide keyboard when clicking on other views
        setupHideKeyboardOnTouch(btnDrawer);
        setupHideKeyboardOnTouch(txtLocation);
        setupHideKeyboardOnTouch(imageSlider);
    }

    private void setupHideKeyboardOnTouch(View view) {
        view.setOnTouchListener((v, event) -> {
            if (searchBar.isFocused()) {
                searchBar.clearFocus();
                hideKeyboard();
            }
            return false; // Allow other touch events to be processed
        });
    }

    private void showKeyboard() {
        if (getActivity() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(searchBar, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    private void hideKeyboard() {
        if (getActivity() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up the handler to prevent memory leaks
        if (sliderHandler != null) {
            sliderHandler.removeCallbacksAndMessages(null);
        }
    }
}