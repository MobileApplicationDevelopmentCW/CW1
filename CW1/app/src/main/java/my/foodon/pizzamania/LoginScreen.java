package my.foodon.pizzamania;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginScreen extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogle;
    private TextView txtForgetPassword, txtCreateAccount;
    private ProgressBar loginProgress;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private GoogleSignInClient googleSignInClient;

    private final int RC_SIGN_IN = 100;
    private static final String TAG = "LoginScreen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        // ✅ Change Status Bar Color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.black_background)); // #060606
        }


        // Bind views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        loginProgress = findViewById(R.id.loginProgress);
        btnGoogle = findViewById(R.id.btnGoogle);
        txtForgetPassword = findViewById(R.id.txtForgetPassword);
        txtCreateAccount = findViewById(R.id.txtSignup);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("Users");

        // Google Sign-In configuration
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Click listeners
        btnLogin.setOnClickListener(v -> loginUser());

        txtForgetPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginScreen.this, ForgotPassword.class);
            String email = etEmail.getText().toString().trim();
            if (!TextUtils.isEmpty(email)) {
                intent.putExtra("email", email);
            }
            startActivity(intent);
        });

        txtCreateAccount.setOnClickListener(v ->
                startActivity(new Intent(LoginScreen.this, CreateAccount.class))
        );

        btnGoogle.setOnClickListener(v -> signInWithGoogle());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter valid email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Enter password");
            return;
        }

        // Admin credentials check (two branches)
        if (email.equals("colombo.admin@gmail.com") && password.equals("admin123")) {
            BranchSession.setBranch(this, BranchSession.BRANCH_COLOMBO);
            Intent intent = new Intent(LoginScreen.this, AdminActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        if (email.equals("galle.admin@gmail.com") && password.equals("admin123")) {
            BranchSession.setBranch(this, BranchSession.BRANCH_GALLE);
            Intent intent = new Intent(LoginScreen.this, AdminActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Disable fields and show loading
        setLoginState(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            // Check user status before allowing login
                            checkUserStatus(user.getUid(), user.getEmail());
                        } else {
                            setLoginState(true);
                            Toast.makeText(LoginScreen.this,
                                    "Please verify your email before login.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        setLoginState(true);
                        Toast.makeText(LoginScreen.this,
                                "Email or password incorrect", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkUserStatus(String uid, String email) {
        Log.d(TAG, "Checking status for UID: " + uid + ", Email: " + email);

        dbRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    CreateAccount.User user = dataSnapshot.getValue(CreateAccount.User.class);
                    if (user != null) {
                        String status = user.status;
                        Log.d(TAG, "User status: " + status);

                        if ("active".equals(status)) {
                            // User is active, allow login
                            setLoginState(true);
                            startActivity(new Intent(LoginScreen.this, MainActivity.class));
                            finish();
                        } else if ("blocked".equals(status)) {
                            // User is blocked
                            mAuth.signOut();
                            setLoginState(true);
                            Toast.makeText(LoginScreen.this,
                                    "Your account has been blocked by Pizza Mania. Please contact support.",
                                    Toast.LENGTH_LONG).show();
                        } else if ("suspended".equals(status)) {
                            // User is suspended
                            mAuth.signOut();
                            setLoginState(true);
                            Toast.makeText(LoginScreen.this,
                                    "Your account has been suspended by Pizza Mania. Please contact support.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            // Unknown status, treat as blocked for safety
                            mAuth.signOut();
                            setLoginState(true);
                            Toast.makeText(LoginScreen.this,
                                    "Account access restricted. Please contact support.",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // User data not found
                        mAuth.signOut();
                        setLoginState(true);
                        Toast.makeText(LoginScreen.this,
                                "Account data not found. Please contact support.",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    // User document doesn't exist
                    Log.w(TAG, "User document doesn't exist for UID: " + uid);
                    mAuth.signOut();
                    setLoginState(true);
                    Toast.makeText(LoginScreen.this,
                            "Account not found. Please contact support.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                setLoginState(true);
                Toast.makeText(LoginScreen.this,
                        "Error checking account status. Please try again.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoginState(boolean enabled) {
        btnLogin.setEnabled(enabled);
        btnLogin.setText(enabled ? "Log in" : "");
        etEmail.setEnabled(enabled);
        etPassword.setEnabled(enabled);
        loginProgress.setVisibility(enabled ? View.GONE : View.VISIBLE);
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data)
                        .getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    // Check user status for Google sign-in too
                    checkUserStatus(user.getUid(), user.getEmail());
                } else {
                    Toast.makeText(LoginScreen.this,
                            "Authentication Failed.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(LoginScreen.this,
                        "Authentication Failed.", Toast.LENGTH_LONG).show();
            }
        });
    }
}