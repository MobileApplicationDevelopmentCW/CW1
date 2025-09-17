package my.foodon.pizzamania.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.method.KeyListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import my.foodon.pizzamania.LoginScreen;
import my.foodon.pizzamania.R;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 101;

    private ImageView imgProfile;
    private EditText etName, etEmail, etPhone;
    private MaterialButton btnChangePic, btnSave;
    private ProgressBar progressBarSave;

    private KeyListener etNameKeyListener, etPhoneKeyListener;
    private Uri imageUri;

    // Variables to store original values for comparison
    private String originalName = "";
    private String originalPhone = "";

    private DatabaseReference dbRef;
    private StorageReference storageRef;
    private FirebaseUser currentUser;

    public ProfileFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(requireActivity(), LoginScreen.class));
            requireActivity().finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imgProfile = view.findViewById(R.id.imgProfile);
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        btnChangePic = view.findViewById(R.id.btnChangePic);
        btnSave = view.findViewById(R.id.btnSave);
        progressBarSave = view.findViewById(R.id.progressBarSave);

        etNameKeyListener = etName.getKeyListener();
        etPhoneKeyListener = etPhone.getKeyListener();

        etName.setKeyListener(null);
        etName.setCursorVisible(false);
        etPhone.setKeyListener(null);
        etPhone.setCursorVisible(false);

        etName.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) enableEditText(etName, etNameKeyListener);
            return false;
        });

        etPhone.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) enableEditText(etPhone, etPhoneKeyListener);
            return false;
        });

        btnChangePic.setOnClickListener(v -> openGallery());
        btnSave.setOnClickListener(v -> saveChanges());

        setupUIToHideKeyboard(view);
        return view;
    }

    private void enableEditText(EditText editText, KeyListener keyListener) {
        editText.setKeyListener(keyListener);
        editText.setCursorVisible(true);
        editText.requestFocus();
        editText.setSelection(editText.getText().length());
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUserListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh drawer header when fragment becomes visible
        if (currentUser != null) {
            new Handler().postDelayed(() -> {
                String name = etName != null ? etName.getText().toString() : "";
                String email = etEmail != null ? etEmail.getText().toString() : "";
                updateDrawerHeader(name, email, null);
            }, 100);
        }
    }

    private void setupUserListener() {
        if (currentUser == null || !isAdded() || getActivity() == null) return;

        dbRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        storageRef = FirebaseStorage.getInstance().getReference("profile_pics");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check if fragment is still attached
                if (!isAdded() || getActivity() == null) return;

                String name = null, email = null, phone = null, profileImageUrl = null;

                if (snapshot.exists()) {
                    name = snapshot.child("name").getValue(String.class);
                    email = snapshot.child("email").getValue(String.class);
                    phone = snapshot.child("phone").getValue(String.class);
                    profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                }

                // Fallback to FirebaseAuth data if database is empty
                if ((name == null || name.isEmpty()) && currentUser.getDisplayName() != null)
                    name = currentUser.getDisplayName();
                if ((email == null || email.isEmpty()) && currentUser.getEmail() != null)
                    email = currentUser.getEmail();

                // Store original values for comparison
                originalName = name != null ? name : "";
                originalPhone = phone != null ? phone : "";
                final String finalEmail = email != null ? email : "";
                final String finalProfileUrl = profileImageUrl;

                // Update profile fragment UI
                if (etName != null) etName.setText(originalName);
                if (etEmail != null) etEmail.setText(finalEmail);
                if (etPhone != null) etPhone.setText(originalPhone);

                // Update profile image in fragment
                if (imgProfile != null && finalProfileUrl != null && !finalProfileUrl.isEmpty()) {
                    try {
                        Glide.with(ProfileFragment.this)
                                .load(finalProfileUrl)
                                .placeholder(R.drawable.user1)
                                .error(R.drawable.user1)
                                .into(imgProfile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Update drawer header - USE POST TO ENSURE UI IS READY
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        new Handler().postDelayed(() -> {
                            updateDrawerHeader(originalName, finalEmail, finalProfileUrl);
                        }, 150);
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded() && getActivity() != null) {
                    Toast.makeText(getActivity(), "Failed to load user data.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateDrawerHeader(String name, String email, String profileImageUrl) {
        try {
            // Multiple safety checks
            if (getActivity() == null || !isAdded()) {
                return;
            }

            // Find the NavigationView in the activity
            DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawerLayout);
            if (drawerLayout == null) {
                return;
            }

            NavigationView navigationView = getActivity().findViewById(R.id.navigationView);
            if (navigationView == null) {
                return;
            }

            // Get header view - make sure it exists
            if (navigationView.getHeaderCount() == 0) {
                return;
            }

            View headerView = navigationView.getHeaderView(0);
            if (headerView == null) {
                return;
            }

            // Find the views in the header
            ImageView imgDrawerProfile = headerView.findViewById(R.id.imgDrawerProfile);
            TextView tvDrawerName = headerView.findViewById(R.id.tvDrawerName);
            TextView tvDrawerEmail = headerView.findViewById(R.id.tvDrawerEmail);

            // Update name
            if (tvDrawerName != null) {
                tvDrawerName.setText(name != null && !name.isEmpty() ? name : "Name");
            }

            // Update email
            if (tvDrawerEmail != null) {
                tvDrawerEmail.setText(email != null && !email.isEmpty() ? email : "Email");
            }

            // Update profile image - FIXED VERSION
            if (imgDrawerProfile != null) {
                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    try {
                        // Debug log to check URL
                        android.util.Log.d("ProfileFragment", "Loading profile image: " + profileImageUrl);

                        // Use getActivity() instead of 'this' for context
                        Glide.with(getActivity())
                                .load(profileImageUrl)
                                .placeholder(R.drawable.user1)
                                .error(R.drawable.user1)
                                .centerCrop()
                                .into(imgDrawerProfile);
                    } catch (Exception e) {
                        // Fallback if Glide fails
                        imgDrawerProfile.setImageResource(R.drawable.user1);
                        android.util.Log.e("ProfileFragment", "Error loading profile image: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    // No profile image URL, use default
                    imgDrawerProfile.setImageResource(R.drawable.user1);
                    android.util.Log.d("ProfileFragment", "No profile image URL, using default");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.e("ProfileFragment", "Error updating drawer header: " + e.getMessage());
        }
    }

    private void openGallery() {
        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                imgProfile.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveChanges() {
        String newName = etName.getText().toString().trim();
        String newPhone = etPhone.getText().toString().trim();

        if (newName.isEmpty()) { etName.setError("Enter name"); return; }
        if (newPhone.isEmpty()) { etPhone.setError("Enter phone number"); return; }

        // Check if any changes have been made
        boolean nameChanged = !newName.equals(originalName);
        boolean phoneChanged = !newPhone.equals(originalPhone);
        boolean imageChanged = imageUri != null;

        if (!nameChanged && !phoneChanged && !imageChanged) {
            Toast.makeText(getActivity(), "No changes to update", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBarSave.setVisibility(View.VISIBLE);
        btnSave.setText("");
        btnSave.setEnabled(false);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("phone", newPhone);

        if (imageUri != null) {
            StorageReference fileRef = storageRef.child(currentUser.getUid() + ".jpg");
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                updates.put("profileImageUrl", uri.toString());
                                updateDatabase(updates);
                            }).addOnFailureListener(this::handleUploadFailure)
                    ).addOnFailureListener(this::handleUploadFailure);
        } else {
            updateDatabase(updates);
        }
    }

    private void updateDatabase(Map<String, Object> updates) {
        dbRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Profile updated", Toast.LENGTH_SHORT).show();
                    // Update original values after successful update
                    originalName = etName.getText().toString().trim();
                    originalPhone = etPhone.getText().toString().trim();

                    imageUri = null;
                    hideProgressBar();

                    // Refresh drawer header after successful update
                    new Handler().postDelayed(() -> {
                        String email = etEmail.getText().toString().trim();
                        updateDrawerHeader(originalName, email, null);
                    }, 500);
                })
                .addOnFailureListener(this::handleUploadFailure);
    }

    private void handleUploadFailure(Exception e) {
        hideProgressBar();
        Toast.makeText(getActivity(),
                "Failed to update profile. Please try again.\nError: " + e.getMessage(),
                Toast.LENGTH_LONG).show();
        imageUri = null;
    }

    private void hideProgressBar() {
        progressBarSave.setVisibility(View.GONE);
        btnSave.setText("Save Changes");
        btnSave.setEnabled(true);
    }

    private void setupUIToHideKeyboard(View view) {
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> { hideKeyboard(); return false; });
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                setupUIToHideKeyboard(((ViewGroup) view).getChildAt(i));
            }
        }
    }

    private void hideKeyboard() {
        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity()
                    .getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }
}