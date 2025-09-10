package my.foodon.pizzamania;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

import my.foodon.pizzamania.fragments.CartFragment;
import my.foodon.pizzamania.fragments.HomeFragment;
import my.foodon.pizzamania.fragments.OrderFragment;
import my.foodon.pizzamania.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private final Fragment homeFragment = new HomeFragment();
    private final Fragment cartFragment = new CartFragment();
    private final Fragment orderFragment = new OrderFragment();
    private final Fragment profileFragment = new ProfileFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fix status bar color to match app theme (#090909)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#090909"));

            // Make status bar icons light (white) since background is dark
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decor = window.getDecorView();
                decor.setSystemUiVisibility(0); // Clear light status bar flag for dark background
            }
        }

        // CHANGE THIS: Use the drawer layout instead of bottom nav layout
        setContentView(R.layout.activity_drawer);

        // Initialize drawer components
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // Initialize bottom navigation (make sure it exists in activity_drawer.xml)
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set default fragment
        switchFragment(homeFragment);

        // Handle bottom navigation
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.home) {
                        switchFragment(homeFragment);
                        return true;
                    } else if (id == R.id.cart) {
                        switchFragment(cartFragment);
                        return true;
                    } else if (id == R.id.order) {
                        switchFragment(orderFragment);
                        return true;
                    } else if (id == R.id.profile) {
                        switchFragment(profileFragment);
                        return true;
                    }

                    return false;
                }
            });
        }

        // Handle drawer navigation
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.navFav) {
                        // Handle favourites
                        // switchFragment(favouritesFragment);
                    } else if (id == R.id.navOrderhistory) {
                        // Handle order history
                        // switchFragment(orderHistoryFragment);
                    } else if (id == R.id.navPolicies) {
                        // Handle policies
                        // switchFragment(policiesFragment);
                    } else if (id == R.id.navLogout) {
                        // Handle logout
                        handleLogout();
                    } else if (id == R.id.navExit) {
                        // Handle exit
                        finish();
                    }

                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
            });
        }
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment) // CHANGE THIS: Use fragmentContainer from activity_drawer.xml
                .commit();
    }

    private void handleLogout() {
        // Add your logout logic here
        // For example:
        // FirebaseAuth.getInstance().signOut();
        // Intent intent = new Intent(this, LoginScreen.class);
        // startActivity(intent);
        // finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}