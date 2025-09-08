package my.foodon.pizzamania;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSend;
    private ProgressBar progressBar;
    private ImageView imgBack;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Bind views
        etEmail = findViewById(R.id.etEmail);
        btnSend = findViewById(R.id.btnSend);
        progressBar = findViewById(R.id.progressBar);
        imgBack = findViewById(R.id.imgBack);

        // Firebase
        mAuth = FirebaseAuth.getInstance();

        // Back button
        imgBack.setOnClickListener(v -> onBackPressed());

        // Prefill email if passed from LoginScreen
        String email = getIntent().getStringExtra("email");
        if (email != null) {
            etEmail.setText(email);
        }

        // Send button click
        btnSend.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            return;
        }

        // Disable input + show progress
        etEmail.setEnabled(false);
        btnSend.setEnabled(false);
        btnSend.setText("");
        progressBar.setVisibility(View.VISIBLE);

        // Send password reset email
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    btnSend.setText("Send");
                    etEmail.setEnabled(true);
                    btnSend.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPassword.this,
                                "Password reset email sent. Check your inbox.",
                                Toast.LENGTH_LONG).show();
                        finish(); // return to login screen
                    } else {
                        Toast.makeText(ForgotPassword.this,
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
