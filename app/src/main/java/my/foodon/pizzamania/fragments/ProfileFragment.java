package my.foodon.pizzamania.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import my.foodon.pizzamania.R;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 101;

    private ImageView imgProfile;
    private EditText etName, etEmail, etPhone;
    private Button btnChangePic, btnSave;

    private Uri imageUri;

    private DatabaseReference dbRef;
    private StorageReference storageRef;
    private FirebaseUser currentUser;

    public ProfileFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Not logged in, go to login screen
            Intent intent = new Intent(requireActivity(), my.foodon.pizzamania.LoginScreen.class);
            startActivity(intent);
            requireActivity().finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Bind views
        imgProfile = view.findViewById(R.id.imgProfile);
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        btnChangePic = view.findViewById(R.id.btnChangePic);
        btnSave = view.findViewById(R.id.btnSave);

        btnChangePic.setOnClickListener(v -> openGallery());
        btnSave.setOnClickListener(v -> saveChanges());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (currentUser != null) {
            dbRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
            storageRef = FirebaseStorage.getInstance().getReference("profile_pics");
            loadUserData();
        }
    }

    private void loadUserData() {
        dbRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String name = snapshot.child("name").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String phone = snapshot.child("phone").getValue(String.class);
                String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                etName.setText(name);
                etEmail.setText(email);
                etPhone.setText(phone);

                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    Glide.with(this).load(profileImageUrl).into(imgProfile);
                }

                // Update drawer header
                updateDrawerHeader(name, email, profileImageUrl);
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
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

        if (newName.isEmpty()) {
            etName.setError("Enter name");
            return;
        }

        if (newPhone.isEmpty()) {
            etPhone.setError("Enter phone number");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("phone", newPhone);

        if (imageUri != null) {
            StorageReference fileRef = storageRef.child(currentUser.getUid() + ".jpg");
            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        updates.put("profileImageUrl", uri.toString());
                        dbRef.updateChildren(updates).addOnSuccessListener(aVoid -> {
                            Toast.makeText(getActivity(), "Profile updated", Toast.LENGTH_SHORT).show();
                            // Update drawer header
                            updateDrawerHeader(newName, etEmail.getText().toString(), uri.toString());
                        });
                    })
            ).addOnFailureListener(e ->
                    Toast.makeText(getActivity(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        } else {
            dbRef.updateChildren(updates).addOnSuccessListener(aVoid -> {
                Toast.makeText(getActivity(), "Profile updated", Toast.LENGTH_SHORT).show();
                // Update drawer header
                updateDrawerHeader(newName, etEmail.getText().toString(), null);
            });
        }
    }


    private void updateDrawerHeader(String name, String email, String profileUrl) {
        // Make sure this ID matches your NavigationView in the activity layout
        NavigationView navigationView = requireActivity().findViewById(R.id.nav_view);
        if (navigationView == null) return;

        View headerView = navigationView.getHeaderView(0);

        ImageView imgDrawerProfile = headerView.findViewById(R.id.imgDrawerProfile);
        TextView tvDrawerName = headerView.findViewById(R.id.tvDrawerName);
        TextView tvDrawerEmail = headerView.findViewById(R.id.tvDrawerEmail);

        tvDrawerName.setText(name);
        tvDrawerEmail.setText(email);

        if (profileUrl != null && !profileUrl.isEmpty()) {
            Glide.with(this).load(profileUrl).into(imgDrawerProfile);
        }
    }
}
