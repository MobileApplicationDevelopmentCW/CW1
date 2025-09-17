package my.foodon.pizzamania.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import my.foodon.pizzamania.DatabaseHelper;
import my.foodon.pizzamania.R;

public class CusAboutFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private TextView tvDescription, tvPhone, tvEmail, tvAddress;
    private ImageView ivLogo;

    public CusAboutFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cus_about, container, false);

        // Initialize views
        tvDescription = view.findViewById(R.id.tv_description);
        tvPhone = view.findViewById(R.id.tv_phone);
        tvEmail = view.findViewById(R.id.tv_email);
        tvAddress = view.findViewById(R.id.tv_address);
        ivLogo = view.findViewById(R.id.iv_logo);

        dbHelper = new DatabaseHelper(requireContext());
        loadAboutInfo();

        return view;
    }

    private void loadAboutInfo() {
        Cursor cursor = dbHelper.getAboutInfo();
        if (cursor != null && cursor.moveToFirst()) {
            int descIndex = cursor.getColumnIndex("description");
            int phoneIndex = cursor.getColumnIndex("phone");
            int emailIndex = cursor.getColumnIndex("email");
            int addressIndex = cursor.getColumnIndex("address");

            if (descIndex != -1) tvDescription.setText(cursor.getString(descIndex));
            if (phoneIndex != -1) tvPhone.setText("📞 " + cursor.getString(phoneIndex));
            if (emailIndex != -1) tvEmail.setText("📧 " + cursor.getString(emailIndex));
            if (addressIndex != -1) tvAddress.setText("📍 " + cursor.getString(addressIndex));

            ivLogo.setImageResource(R.drawable.placeholder);
        }
        if (cursor != null) cursor.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAboutInfo();
    }
}
