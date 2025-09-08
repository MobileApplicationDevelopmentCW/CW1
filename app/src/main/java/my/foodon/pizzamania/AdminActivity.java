package my.foodon.pizzamania;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import my.foodon.pizzamania.adfragments.MenuManageFragment;
import my.foodon.pizzamania.adfragments.OrderManageFragment;
import my.foodon.pizzamania.adfragments.UserManageFragment;

public class AdminActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private final Fragment menuManageFragment = new MenuManageFragment();
    private final Fragment adminUserFragment = new UserManageFragment();
    private final Fragment adminOrderFragment = new OrderManageFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_nav_admin);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

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

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}
