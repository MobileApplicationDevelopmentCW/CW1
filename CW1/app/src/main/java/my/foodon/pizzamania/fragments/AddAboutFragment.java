package my.foodon.pizzamania.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import my.foodon.pizzamania.R;
import my.foodon.pizzamania.DatabaseHelper; // Make sure this is your correct package

public class AddAboutFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private EditText etTitle, etDescription, etImageUrl, etPhone, etEmail, etAddress;
    private Button btnUpdate, btnDelete, btnInsert;

    public AddAboutFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ad_about, container, false);

        // Initialize views
        etTitle = view.findViewById(R.id.et_title);
        etDescription = view.findViewById(R.id.et_description);
        etImageUrl = view.findViewById(R.id.et_image_url);
        etPhone = view.findViewById(R.id.et_phone);
        etEmail = view.findViewById(R.id.et_email);
        etAddress = view.findViewById(R.id.et_address);

        btnUpdate = view.findViewById(R.id.btn_update);
        btnDelete = view.findViewById(R.id.btn_delete);
        btnInsert = view.findViewById(R.id.btn_insert);

        dbHelper = new DatabaseHelper(getActivity());
        loadCurrentData();
        setupClickListeners();

        return view;
    }

    private void setupClickListeners() {
        btnUpdate.setOnClickListener(v -> updateAboutInfo());
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
        btnInsert.setOnClickListener(v -> insertAboutInfo());
    }

    private void loadCurrentData() {
        Cursor cursor = dbHelper.getAboutInfo();
        if (cursor != null && cursor.moveToFirst()) {
            etTitle.setText(cursor.getString(cursor.getColumnIndex("title")));
            etDescription.setText(cursor.getString(cursor.getColumnIndex("description")));
            etImageUrl.setText(cursor.getString(cursor.getColumnIndex("image_url")));
            etPhone.setText(cursor.getString(cursor.getColumnIndex("phone")));
            etEmail.setText(cursor.getString(cursor.getColumnIndex("email")));
            etAddress.setText(cursor.getString(cursor.getColumnIndex("address")));
            cursor.close();
        }
    }

    private void updateAboutInfo() {
        if (validateFields()) {
            boolean result = dbHelper.updateAboutInfo(
                    etTitle.getText().toString(),
                    etDescription.getText().toString(),
                    etImageUrl.getText().toString(),
                    etPhone.getText().toString(),
                    etEmail.getText().toString(),
                    etAddress.getText().toString()
            );

            Toast.makeText(getActivity(),
                    result ? "About information updated successfully!" : "Failed to update information",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void insertAboutInfo() {
        if (validateFields()) {
            boolean result = dbHelper.insertAboutInfo(
                    etTitle.getText().toString(),
                    etDescription.getText().toString(),
                    etImageUrl.getText().toString(),
                    etPhone.getText().toString(),
                    etEmail.getText().toString(),
                    etAddress.getText().toString()
            );

            Toast.makeText(getActivity(),
                    result ? "New about information inserted successfully!" : "Failed to insert information",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Delete Confirmation")
                .setMessage("Are you sure you want to delete all about information?")
                .setPositiveButton("Yes", (dialog, which) -> deleteAboutInfo())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteAboutInfo() {
        boolean result = dbHelper.deleteAboutInfo();
        Toast.makeText(getActivity(),
                result ? "About information deleted successfully!" : "Failed to delete information",
                Toast.LENGTH_SHORT).show();
        if (result) clearFields();
    }

    private boolean validateFields() {
        if (etTitle.getText().toString().trim().isEmpty() ||
                etDescription.getText().toString().trim().isEmpty()) {
            Toast.makeText(getActivity(), "Title and Description are required!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void clearFields() {
        etTitle.setText("");
        etDescription.setText("");
        etImageUrl.setText("");
        etPhone.setText("");
        etEmail.setText("");
        etAddress.setText("");
    }
}
