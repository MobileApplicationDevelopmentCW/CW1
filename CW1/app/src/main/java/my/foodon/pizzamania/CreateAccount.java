package my.foodon.pizzamania;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateAccount extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword, etPhone;
    private Button btnCreateAccount;
    private ProgressBar progressBar;
    private TextView txtSignin;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // ✅ Change Status Bar Color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.black_background)); // #060606
        }

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("Users");

        // Bind Views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etPhone = findViewById(R.id.etPhone);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        progressBar = findViewById(R.id.progressBar);
        txtSignin = findViewById(R.id.txtSignin);

        // Button click
        btnCreateAccount.setOnClickListener(v -> registerUser());

        // Sign In click
        txtSignin.setOnClickListener(v -> {
            startActivity(new Intent(CreateAccount.this, LoginScreen.class));
            finish();
        });
    }

    private void registerUser() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Enter your name");
            return;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 8) {
            etPassword.setError("Password must be at least 8 characters");
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }
        if (TextUtils.isEmpty(phone) || phone.length() < 10) {
            etPhone.setError("Enter a valid phone number");
            return;
        }

        // Disable inputs and show progress (hide button text)
        setInputsEnabled(false);
        btnCreateAccount.setText("");
        progressBar.setVisibility(View.VISIBLE);

        // Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            firebaseUser.sendEmailVerification()
                                    .addOnCompleteListener(emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            // Save user in DB with UID as key
                                            String uid = firebaseUser.getUid();
                                            User user = new User(uid, fullName, email, phone); // Include UID
                                            dbRef.child(uid).setValue(user);

                                            Toast.makeText(CreateAccount.this,
                                                    "Account created! Please verify your email.",
                                                    Toast.LENGTH_LONG).show();

                                            // Reset button before redirect
                                            btnCreateAccount.setText("Create Account");
                                            progressBar.setVisibility(View.GONE);

                                            mAuth.signOut();
                                            startActivity(new Intent(CreateAccount.this, LoginScreen.class));
                                            finish();
                                        } else {
                                            showError("Error sending verification email: " +
                                                    emailTask.getException().getMessage());
                                        }
                                    });
                        }
                    } else {
                        showError("Error: " + task.getException().getMessage());
                    }
                });
    }

    private void showError(String message) {
        Toast.makeText(CreateAccount.this, message, Toast.LENGTH_LONG).show();
        setInputsEnabled(true);
        progressBar.setVisibility(View.GONE);
        btnCreateAccount.setText("Create Account");
    }

    private void setInputsEnabled(boolean enabled) {
        etFullName.setEnabled(enabled);
        etEmail.setEnabled(enabled);
        etPassword.setEnabled(enabled);
        etConfirmPassword.setEnabled(enabled);
        etPhone.setEnabled(enabled);
        btnCreateAccount.setEnabled(enabled);
    }

    // Updated User Model
    public static class User {
        public String uid, name, email, phone, profileImageUrl, status;

        public User() { }

        public User(String uid, String name, String email, String phone) {
            this.uid = uid;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.profileImageUrl = "";
            this.status = "active";
        }
    }
}