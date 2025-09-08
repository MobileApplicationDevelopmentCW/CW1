package my.foodon.pizzamania.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
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

        // Save original KeyListeners
        etNameKeyListener = etName.getKeyListener();
        etPhoneKeyListener = etPhone.getKeyListener();

        // Make Name & Phone read-only initially
        etName.setKeyListener(null);
        etName.setCursorVisible(false);
        etPhone.setKeyListener(null);
        etPhone.setCursorVisible(false);

        // Tap-to-edit
        etName.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                enableEditText(etName, etNameKeyListener);
            }
            return false;
        });

        etPhone.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                enableEditText(etPhone, etPhoneKeyListener);
            }
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

        InputMethodManager imm = (InputMethodManager) requireActivity()
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUserListener();
    }

    // ------------------- LIVE USER DATA LISTENER -------------------
    private void setupUserListener() {
        if (currentUser == null) return;

        dbRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        storageRef = FirebaseStorage.getInstance().getReference("profile_pics");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = null, email = null, phone = null, profileImageUrl = null;

                if (snapshot.exists()) {
                    name = snapshot.child("name").getValue(String.class);
                    email = snapshot.child("email").getValue(String.class);
                    phone = snapshot.child("phone").getValue(String.class);
                    profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                }

                // If DB didn’t have values, fallback to FirebaseAuth user
                if ((name == null || name.isEmpty()) && currentUser.getDisplayName() != null) {
                    name = currentUser.getDisplayName();
                }
                if ((email == null || email.isEmpty()) && currentUser.getEmail() != null) {
                    email = currentUser.getEmail();
                }

                etName.setText(name != null ? name : "");
                etEmail.setText(email != null ? email : "");
                etPhone.setText(phone != null ? phone : "");

                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    Glide.with(ProfileFragment.this).load(profileImageUrl).into(imgProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
        });
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

    // ------------------- SAVE CHANGES -------------------
    private void saveChanges() {
        String newName = etName.getText().toString().trim();
        String newPhone = etPhone.getText().toString().trim();

        if (newName.isEmpty()) { etName.setError("Enter name"); return; }
        if (newPhone.isEmpty()) { etPhone.setError("Enter phone number"); return; }

        progressBarSave.setVisibility(View.VISIBLE);
        btnSave.setText("");
        btnSave.setEnabled(false);

        dbRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) { hideProgressBar(); return; }

            String currentName = snapshot.child("name").getValue(String.class);
            String currentPhone = snapshot.child("phone").getValue(String.class);

            Map<String, Object> updates = new HashMap<>();
            boolean hasChanges = false;

            if (!newName.equals(currentName)) { updates.put("name", newName); hasChanges = true; }
            if (!newPhone.equals(currentPhone)) { updates.put("phone", newPhone); hasChanges = true; }

            if (imageUri != null) {
                hasChanges = true;
                StorageReference fileRef = storageRef.child(currentUser.getUid() + ".jpg");

                fileRef.putFile(imageUri)
                        .addOnSuccessListener(taskSnapshot ->
                                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    updates.put("profileImageUrl", uri.toString());
                                    dbRef.updateChildren(updates).addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getActivity(), "Profile updated", Toast.LENGTH_SHORT).show();
                                        imageUri = null;
                                        hideProgressBar();
                                    }).addOnFailureListener(this::handleUploadFailure);
                                }).addOnFailureListener(this::handleUploadFailure)
                        ).addOnFailureListener(this::handleUploadFailure);

            } else if (hasChanges) {
                dbRef.updateChildren(updates).addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Profile updated", Toast.LENGTH_SHORT).show();
                    hideProgressBar();
                }).addOnFailureListener(this::handleUploadFailure);
            } else {
                Toast.makeText(getActivity(), "No changes to update", Toast.LENGTH_SHORT).show();
                hideProgressBar();
            }
        }).addOnFailureListener(this::handleUploadFailure);
    }

    private void handleUploadFailure(Exception e) {
        hideProgressBar();
        Toast.makeText(getActivity(),
                "Profile upload failed. Please try again.\nError: " + e.getMessage(),
                Toast.LENGTH_LONG).show();

        imageUri = null;

        requireActivity().getSupportFragmentManager().beginTransaction()
                .detach(this)
                .attach(this)
                .commit();
    }

    private void hideProgressBar() {
        progressBarSave.setVisibility(View.GONE);
        btnSave.setText("Save Changes");
        btnSave.setEnabled(true);
    }

    // ------------------- KEYBOARD HIDING -------------------
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
