package my.foodon.pizzamania.adfragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import my.foodon.pizzamania.R;
import my.foodon.pizzamania.models.Driver;

public class DriverManageFragment extends Fragment {

    private EditText etDname, etDplate, etDtel;
    private Spinner spBranch;
    private Button btnSave;
    private DatabaseReference driversRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_manage, container, false);

        etDname = view.findViewById(R.id.etDname);
        etDplate = view.findViewById(R.id.etDplate);
        etDtel = view.findViewById(R.id.etDtel);
        spBranch = view.findViewById(R.id.spBranch);
        btnSave = view.findViewById(R.id.btnSaveDriver);

        ArrayAdapter<String> branchAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item2,
                new String[]{"colombo", "galle"});
        branchAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item2);
        spBranch.setAdapter(branchAdapter);

        driversRef = FirebaseDatabase.getInstance().getReference("drivers");

        btnSave.setOnClickListener(v -> saveDriver());

        return view;
    }

    private void saveDriver() {
        String dname = etDname.getText().toString().trim();
        String dplate = etDplate.getText().toString().trim();
        String dtel = etDtel.getText().toString().trim();
        String dbranch = (String) spBranch.getSelectedItem();

        if (TextUtils.isEmpty(dname) || TextUtils.isEmpty(dplate) || TextUtils.isEmpty(dtel)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // allocate auto-increment id using transaction on drivers_meta/nextId
        DatabaseReference metaRef = FirebaseDatabase.getInstance().getReference("drivers_meta/nextId");
        metaRef.runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @NonNull
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(@NonNull com.google.firebase.database.MutableData currentData) {
                Long value = currentData.getValue(Long.class);
                if (value == null) value = 1L;
                currentData.setValue(value + 1);
                return com.google.firebase.database.Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable com.google.firebase.database.DataSnapshot currentData) {
                if (!committed || error != null) {
                    Toast.makeText(getContext(), "Failed to generate ID: " + (error != null ? error.getMessage() : ""), Toast.LENGTH_SHORT).show();
                    return;
                }
                long allocated = currentData != null && currentData.getValue() != null ? (Long) currentData.getValue() : 1L;
                long didNum = allocated - 1; // we incremented to next, so current is -1
                String did = String.format("D%03d", didNum);

                Driver d = new Driver(did, dname, dplate, dtel);
                d.dbranch = dbranch;

                driversRef.child(did)
                        .setValue(d)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Driver saved", Toast.LENGTH_SHORT).show();
                            etDname.setText("");
                            etDplate.setText("");
                            etDtel.setText("");
                            spBranch.setSelection(0);
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
}


