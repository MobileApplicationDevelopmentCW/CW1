package my.foodon.pizzamania;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import my.foodon.pizzamania.fragments.CartFragment;
import my.foodon.pizzamania.fragments.HomeFragment;
import my.foodon.pizzamania.fragments.OrderFragment;
import my.foodon.pizzamania.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private final Fragment homeFragment = new HomeFragment();
    private final Fragment cartFragment = new CartFragment();
    private final Fragment orderFragment = new OrderFragment();
    private final Fragment profileFragment = new ProfileFragment();


    private final Fragment menuFragment = new Fragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_nav);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set default fragment
        switchFragment(homeFragment);

        // Handle bottom navigation
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

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}
