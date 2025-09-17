package my.foodon.pizzamania.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import my.foodon.pizzamania.R;
import my.foodon.pizzamania.BranchSession;
import my.foodon.pizzamania.MainActivity;
import my.foodon.pizzamania.MapsActivity; // Add this import
import my.foodon.pizzamania.adapters.ImageSliderAdapter;
import my.foodon.pizzamania.fragments.ChatbotFragment;

public class HomeFragment extends Fragment {

    private ImageButton btnDrawer;
    private EditText searchBar;
    private TextView txtLocation;
    private ViewPager2 imageSlider;
    private Handler sliderHandler = new Handler();
    private View rootView;
    private LinearLayout mainContainer;
    private boolean isKeyboardVisible = false;

    private TextView tvSelectedLocation;
    private CardView locationCard;
    private double[] currentSelectedLocation = null;
    private Geocoder geocoder;
    private FloatingActionButton fabChatbot;

    // Activity result launcher for the map activity
    private ActivityResultLauncher<Intent> mapActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    currentSelectedLocation = result.getData().getDoubleArrayExtra(MapsActivity.EXTRA_SELECTED_LOCATION);
                    updateLocationDisplay();
                    applyNearestBranchAndReloadMenu();
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main_home, container, false);
        rootView = view;

        // ✅In Fragment Set Status Bar & Navigation Bar color to #060606
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getActivity() != null) {
            getActivity().getWindow().setStatusBarColor(
                    getResources().getColor(R.color.black_background)
            );
            getActivity().getWindow().setNavigationBarColor(
                    getResources().getColor(R.color.black_background)
            );
        }

        // Initialize views
        btnDrawer = view.findViewById(R.id.btnDrawer);
        searchBar = view.findViewById(R.id.searchBar);
        txtLocation = view.findViewById(R.id.tvSelectedLocation);
        tvSelectedLocation = txtLocation; // Same reference for consistency
        imageSlider = view.findViewById(R.id.imageSlider);
        locationCard = view.findViewById(R.id.locationCard);
        fabChatbot = view.findViewById(R.id.fabChatbot);

        // Initialize geocoder
        geocoder = new Geocoder(requireContext(), Locale.getDefault());

        // Setup location functionality
        setupLocationHandling();

        // Setup search bar keyboard handling
        setupSearchBarKeyboard();

        // Setup keyboard visibility detection
        setupKeyboardVisibilityListener();

        // Auto Image Slider setup
        int[] images = {R.drawable.slide1, R.drawable.slide2, R.drawable.slide3};
        ImageSliderAdapter adapter = new ImageSliderAdapter(images);
        imageSlider.setAdapter(adapter);

        // Auto-slide every 5s
        Runnable sliderRunnable = new Runnable() {
            @Override
            public void run() {
                if (imageSlider != null) {
                    int nextItem = (imageSlider.getCurrentItem() + 1) % images.length;
                    imageSlider.setCurrentItem(nextItem, true);
                    sliderHandler.postDelayed(this, 5000);
                }
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

        // Chatbot FAB click listener
        fabChatbot.setOnClickListener(v -> {
            // Switch to chatbot fragment
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.switchFragment(new ChatbotFragment());
            }
        });

        // Load MenuFragment into the container once
        if (savedInstanceState == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.homeMenuContainer, new MenuFragment())
                    .commit();
        }

        return view;
    }

    private void setupLocationHandling() {
        // Set click listener on the location card
        if (locationCard != null) {
            locationCard.setOnClickListener(v -> openMapActivity());
        }

        // Initialize the display and nearest branch
        updateLocationDisplay();
        applyNearestBranchAndReloadMenu();
    }

    private void openMapActivity() {
        Intent intent = new Intent(requireContext(), MapsActivity.class);

        // Pass the previously selected location if it exists
        if (currentSelectedLocation != null) {
            intent.putExtra(MapsActivity.EXTRA_PREVIOUS_LOCATION, currentSelectedLocation);
        }

        mapActivityLauncher.launch(intent);
    }

    private void updateLocationDisplay() {
        if (currentSelectedLocation != null) {
            // Perform reverse geocoding to get address
            getAddressFromCoordinates(currentSelectedLocation[0], currentSelectedLocation[1]);
        } else {
            tvSelectedLocation.setText("Colombo, Sri Lanka");
        }
    }

    private void applyNearestBranchAndReloadMenu() {
        String previousBranch = BranchSession.getBranch(requireContext());
        String nearest = determineNearestBranch();
        if (nearest == null) {
            nearest = BranchSession.BRANCH_COLOMBO;
        }
        if (!nearest.equals(previousBranch)) {
            BranchSession.setBranch(requireContext(), nearest);
            getChildFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.homeMenuContainer, new MenuFragment())
                    .commit();
        } else {
            BranchSession.setBranch(requireContext(), nearest);
        }
    }

    private String determineNearestBranch() {
        // Colombo: 6.9271, 79.8612 | Galle: 6.0535, 80.2210
        double colLat = 6.9271;
        double colLng = 79.8612;
        double galLat = 6.0535;
        double galLng = 80.2210;

        if (currentSelectedLocation == null) {
            return BranchSession.BRANCH_COLOMBO;
        }

        double refLat = currentSelectedLocation[0];
        double refLng = currentSelectedLocation[1];

        double dCol = haversineKm(refLat, refLng, colLat, colLng);
        double dGal = haversineKm(refLat, refLng, galLat, galLng);
        return dCol <= dGal ? BranchSession.BRANCH_COLOMBO : BranchSession.BRANCH_GALLE;
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private void getAddressFromCoordinates(double latitude, double longitude) {
        // Show loading text while geocoding
        tvSelectedLocation.setText("Loading location...");

        // Perform geocoding in a background thread
        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                // Use requireActivity().runOnUiThread instead of runOnUiThread
                requireActivity().runOnUiThread(() -> {
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        String locationText = formatAddress(address);
                        tvSelectedLocation.setText(locationText);
                    } else {
                        // Fallback to coordinates if geocoding fails
                        String fallbackText = String.format(Locale.getDefault(),
                                "Lat: %.4f, Lng: %.4f", latitude, longitude);
                        tvSelectedLocation.setText(fallbackText);
                    }
                });

            } catch (IOException e) {
                requireActivity().runOnUiThread(() -> {
                    // Fallback to coordinates if geocoding fails
                    String fallbackText = String.format(Locale.getDefault(),
                            "Lat: %.4f, Lng: %.4f", latitude, longitude);
                    tvSelectedLocation.setText(fallbackText);
                });
            }
        }).start();
    }

    private String formatAddress(Address address) {
        String district = null;
        String country = null;

        // Try to get district (administrative area)
        if (address.getSubAdminArea() != null) {
            district = address.getSubAdminArea();
        } else if (address.getAdminArea() != null) {
            district = address.getAdminArea();
        } else if (address.getLocality() != null) {
            district = address.getLocality();
        }

        // Get country
        if (address.getCountryName() != null) {
            country = address.getCountryName();
        }

        // Format the display text
        if (district != null && country != null) {
            return district + ", " + country;
        } else if (district != null) {
            return district;
        } else if (country != null) {
            return country;
        } else {
            // Fallback to coordinates or unknown
            return "Unknown location";
        }


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
        if (view != null) {
            view.setOnTouchListener((v, event) -> {
                if (searchBar.isFocused()) {
                    searchBar.clearFocus();
                    hideKeyboard();
                }
                return false; // Allow other touch events to be processed
            });
        }
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