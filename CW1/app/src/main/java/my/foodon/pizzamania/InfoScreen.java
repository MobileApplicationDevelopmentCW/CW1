package my.foodon.pizzamania;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class InfoScreen extends AppCompatActivity {

    private View dot1, dot2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_info_screen);

        // Apply edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize dots
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);

        // Set first dot as active (red), second as inactive (gray)
        dot1.setBackgroundResource(R.drawable.dot_active);
        dot2.setBackgroundResource(R.drawable.dot_inactive);

        // Next button click
        Button btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InfoScreen.this, LoginScreen.class);
                startActivity(intent);
            }
        });
    }
}
