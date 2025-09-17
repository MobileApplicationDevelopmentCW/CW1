package my.foodon.pizzamania;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import my.foodon.pizzamania.adfragments.MenuManageFragment;
import my.foodon.pizzamania.adfragments.OrderManageFragment;
import my.foodon.pizzamania.adfragments.UserManageFragment;
import my.foodon.pizzamania.fragments.AddAboutFragment;
import my.foodon.pizzamania.fragments.PolicyFragment;
import my.foodon.pizzamania.adfragments.DriverManageFragment;

public class AdminActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private final Fragment menuManageFragment = new MenuManageFragment();
    private final Fragment adminUserFragment = new UserManageFragment();
    private final Fragment adminOrderFragment = new OrderManageFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_nav_admin);

        // ✅ Change Status Bar Color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.black_background)); // #060606
        }

        // Initialize views
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Indicate current branch in title for clarity
        String branch = BranchSession.getBranch(this);
        setTitle("Admin - " + (BranchSession.BRANCH_COLOMBO.equals(branch) ? "Colombo" : "Galle"));

        // Set navigation item selected listener
        navigationView.setNavigationItemSelectedListener(this);

        // Show Orders fragment by default
        switchFragment(adminOrderFragment);
        bottomNavigationView.setSelectedItemId(R.id.orders_ad);

        // Handle bottom navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_ad) {
                switchFragment(menuManageFragment);
                return true;
            } else if (id == R.id.user_ad) {
                switchFragment(adminUserFragment);
                return true;
            } else if (id == R.id.orders_ad) {
                switchFragment(adminOrderFragment);
                return true;
            }
            return false;
        });
    }

    private void switchFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    // Method to open drawer (called from fragments)
    public void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.navAddabout) {
            Fragment addAboutFragment = new AddAboutFragment();
            switchFragment(addAboutFragment);
        } else if (id == R.id.navAdPolicies) {
            Fragment policyFragment = new PolicyFragment();
            switchFragment(policyFragment);
        } else if (id == R.id.navAdDrivers) {
            Fragment driverFragment = new DriverManageFragment();
            switchFragment(driverFragment);
        } else if (id == R.id.navAdLogout) {
            handleLogout();
        } else if (id == R.id.navAdExit) {
            // Handle Exit click
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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