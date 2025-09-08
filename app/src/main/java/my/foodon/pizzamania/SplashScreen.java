package my.foodon.pizzamania;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this, InfoScreen.class);
                startActivity(intent);
                finish();
            }
        };
        handler.postDelayed(runnable, 3800);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}