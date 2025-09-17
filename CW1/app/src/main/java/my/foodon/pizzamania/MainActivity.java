package my.foodon.pizzamania;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import android.view.View;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import my.foodon.pizzamania.fav.FavoritesFragment;
import my.foodon.pizzamania.fragments.AddAboutFragment;
import my.foodon.pizzamania.fragments.CartFragment;
import my.foodon.pizzamania.fragments.ChatbotFragment;
import my.foodon.pizzamania.fragments.CusAboutFragment;
import my.foodon.pizzamania.fragments.HomeFragment;
import my.foodon.pizzamania.fragments.OrderFragment;
import my.foodon.pizzamania.fragments.PolicyFragment;
import my.foodon.pizzamania.fragments.ProfileFragment;
import my.foodon.pizzamania.OrderHistoryFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private final Fragment homeFragment = new HomeFragment();
    private final Fragment cartFragment = new CartFragment();
    private final Fragment orderFragment = new OrderFragment();
    private final Fragment profileFragment = new ProfileFragment();
    private final Fragment orderHistoryFragment = new OrderHistoryFragment();
    private final Fragment cusAboutFragment = new CusAboutFragment();
    private final Fragment chatbotFragment = new ChatbotFragment();
    private final Fragment addAboutFragment = new AddAboutFragment();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        // Initialize drawer components
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // Initialize bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set default fragment
        switchFragment(homeFragment);

        // Handle bottom navigation clicks
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

        // Handle drawer navigation with smooth transition
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();

                    // Close the drawer first
                    drawerLayout.closeDrawer(GravityCompat.START);

                    // Add listener to perform action AFTER drawer is fully closed
                    drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                        @Override
                        public void onDrawerClosed(@NonNull View drawerView) {
                            if (id == R.id.navChatbot) {
                                switchFragment(chatbotFragment);
                            } else if (id == R.id.navabout) {
                                switchFragment(cusAboutFragment);
                            } else if (id == R.id.navCust) {
                                Intent intent = new Intent(MainActivity.this, Customize_o.class);
                                startActivity(intent);
                            }
                            else if (id == R.id.navFav) {
                                switchFragment(new FavoritesFragment());
                            } else if (id == R.id.navOrderhistory) {
                                switchFragment(orderHistoryFragment);
                            } else if (id == R.id.navPolicies) {

                                Fragment policyFragment = new PolicyFragment();
                                switchFragment(policyFragment);
                            } else if (id == R.id.navLogout) {
                                handleLogout();
                            } else if (id == R.id.navExit) {
                                finish();
                            }

                            // Remove listener after one use
                            drawerLayout.removeDrawerListener(this);
                        }
                    });

                    return true;
                }
            });
        }
    }

    public void switchFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void handleLogout() {
         FirebaseAuth.getInstance().signOut();
         Intent intent = new Intent(this, LoginScreen.class);
         startActivity(intent);
         finish();
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
